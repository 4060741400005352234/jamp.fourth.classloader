package main;

import loader.JarClassLoader;
import org.apache.log4j.Logger;
import plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UserConsole {

    public static final String PACKAGE_DELIMITER = ".";
    private static Logger log = Logger.getLogger(UserConsole.class);

    private BufferedReader bufferedReader;

    public void performUserControl() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                System.out.println("Enter jar-file name (with path): ");
                String jarFilePath = bufferedReader.readLine();
                File jarFile = new File(jarFilePath);
                if (jarFile.exists() && jarFile.isFile()) {
                    String pluginClassName = null;
                    try {
                        System.out.println("Enter plugin folder (package): ");
                        String pluginPackage = bufferedReader.readLine();
                        System.out.println("Enter plugin class-name: ");
                        pluginClassName = bufferedReader.readLine();
                        // Создаем загрузчик
                        JarClassLoader jarClassLoader = new JarClassLoader(jarFilePath, pluginPackage);
                        // Загружаем класс
                        Class<?> clazz = Class.forName(pluginPackage + PACKAGE_DELIMITER + pluginClassName, true, jarClassLoader);
                        // Создаем экземпляр класса
                        Plugin plugin = (Plugin) clazz.newInstance();
                        plugin.run();
                    } catch (ClassNotFoundException e) {
                        log.error(pluginClassName + " plugin not found in jar.", e);
                    } catch (InstantiationException e) {
                        log.error(e);
                    } catch (IllegalAccessException e) {
                        log.error(e);
                    }
                } else {
                    log.warn("Incorrect jar-file path - " + jarFilePath);
                }
            } finally {
                bufferedReader.close();
            }
        } catch (IOException e) {
            log.error("Error during processing.", e);
        }
    }
}
