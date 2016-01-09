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

    private HashMap<String, Class> cache = new HashMap<String, Class>();
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
                // Validation and loading
                if (isClassInPackage(normalize(jarEntry.getName()), packageName)) {
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

    public synchronized Class loadClass(String name) throws ClassNotFoundException {
        log.info("Loading class " + name);
        Class result = cache.get(name);
        if (result == null) {
            result = cache.get(packageName + "." + name);
        }
        if (result == null) {
            result = super.findSystemClass(name);
        }
        return result;
    }

    private String stripClassName(String className) {
        return className.substring(0, className.length() - 6);
    }

    private String normalize(String className) {
        return className.replace('/', '.');
    }

    private boolean isClassInPackage(String className, String packageName) {
        return className.startsWith(packageName) && className.endsWith(".class");
    }

    private byte[] loadClassData(JarFile jarFile, JarEntry jarEntry) throws IOException {
        long size = jarEntry.getSize();
        if (size <= 0)
            return null;
        byte[] data = new byte[(int) size];
        InputStream in = jarFile.getInputStream(jarEntry);
        try {
            in.read(data);
        } finally {
            in.close();
        }
        return data;
    }
}
