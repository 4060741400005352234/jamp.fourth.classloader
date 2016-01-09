package loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader extends ClassLoader {

    private HashMap<String, Class<?>> cache = new HashMap<String, Class<?>>();
    private String jarFileName;
    private String packageName;

    private static String WARNING = "Warning : No jar file found. Packet unmarshalling won't be possible. Please verify your classpath";

    public JarClassLoader(String jarFileName, String packageName) {
        this.jarFileName = jarFileName;
        this.packageName = packageName;
        cacheClasses();
    }

    private void cacheClasses() {
        try {
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                // Одно из назначений хорошего загрузчика - валидация классов на этапе загрузки
                if (match(normalize(jarEntry.getName()), packageName)) {
                    byte[] classData = loadClassData(jarFile, jarEntry);
                    if (classData != null) {
                        Class<?> clazz = defineClass(stripClassName(normalize(jarEntry.getName())), classData, 0, classData.length);
                        cache.put(clazz.getName(), clazz);
                        System.out.println("== class " + clazz.getName() + " loaded in cache");
                    }
                }
            }
        } catch (IOException IOE) {
            System.out.println(WARNING);
        }
    }

    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = cache.get(name);
        // Возможно класс вызывается не по полному имени - добавим имя пакета
        if (result == null)
            result = cache.get(packageName + "." + name);
        // Если класса нет в кэше то возможно он системный
        if (result == null)
            result = super.findSystemClass(name);
        System.out.println("== loadClass(" + name + ")");
        return result;
    }

    /**
     * Получаем каноническое имя класса
     *
     * @param className
     * @return
     */
    private String stripClassName(String className) {
        return className.substring(0, className.length() - 6);
    }

    /**
     * Преобразуем имя в файловой системе в имя класса
     * <p/>
     * (заменяем слэши на точки)
     *
     * @param className
     * @return
     */
    private String normalize(String className) {
        return className.replace('/', '.');
    }

    /**
     * Валидация класса - проверят принадлежит ли класс заданному пакету и имеет ли
     * <p/>
     * он расширение .class
     *
     * @param className
     * @param packageName
     * @return
     */
    private boolean match(String className, String packageName) {
        return className.startsWith(packageName) && className.endsWith(".class");
    }

    /**
     * Извлекаем файл из заданного JarEntry
     *
     * @param jarFile  - файл jar-архива из которого извлекаем нужный файл
     * @param jarEntry - jar-сущность которую извлекаем
     * @return null если невозможно прочесть файл
     */
    private byte[] loadClassData(JarFile jarFile, JarEntry jarEntry) throws IOException {
        long size = jarEntry.getSize();
        if (size == -1 || size == 0)
            return null;
        byte[] data = new byte[(int) size];
        InputStream in = jarFile.getInputStream(jarEntry);
        in.read(data);
        return data;
    }
}
