package loader;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DynamicPluginLoader extends ClassLoader {

    private static Logger log = Logger.getLogger(DynamicPluginLoader.class);

    private HashMap<String, Class<?>> cache = new HashMap<String, Class<?>>();
    private String jarFileName;
    private String packageName;

    public DynamicPluginLoader(String jarFileName, String packageName) {
        this.jarFileName = jarFileName;
        this.packageName = packageName;
        cacheClasses();
    }

    private void cacheClasses() {
        log.info("Start processing jar " + jarFileName);
        try {
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                log.info("New jar-entry found - " + jarEntry.getName());
                // Одно из назначений хорошего загрузчика - валидация классов на этапе загрузки
                if (match(normalize(jarEntry.getName()), packageName)) {
                    byte[] classData = loadClassData(jarFile, jarEntry);
                    if (classData != null) {
                        Class<?> clazz = defineClass(stripClassName(normalize(jarEntry.getName())), classData, 0, classData.length);
                        cache.put(clazz.getName(), clazz);
                        log.info("Class " + clazz.getName() + " loaded in local cache.");
                    }
                } else {
                    log.info("Jar-entry " + jarEntry.getName() + " is not appropriate for loading.");
                }
            }
        } catch (IOException e) {
            log.error("Error during processing jar-file " + jarFileName, e );
        }
    }

    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        log.info("Loading class " + name);
        Class<?> result = cache.get(name);
        // Возможно класс вызывается не по полному имени - добавим имя пакета
        if (result == null)
            result = cache.get(packageName + "." + name);
        // Если класса нет в кэше то возможно он системный
        if (result == null)
            result = super.findSystemClass(name);
        return result;
    }

    private String stripClassName(String className) {
        return className.substring(0, className.length() - 6);
    }

    private String normalize(String className) {
        return className.replace('/', '.');
    }

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
