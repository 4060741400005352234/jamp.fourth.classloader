package main;

import loader.DynamicPluginLoader;
import org.apache.log4j.Logger;
import plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PluginService {

    public static final String PACKAGE_DELIMITER = ".";

    private static Logger log = Logger.getLogger(PluginService.class);

    public void performLoadPlugin() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                System.out.println("Enter jar-file name (D:/plugin.jar):");
                String jarFilePath = bufferedReader.readLine();
                File jarFile = new File(jarFilePath);
                if (jarFile.exists() && jarFile.isFile()) {
                    System.out.println("Enter plugin folder/package (plugin.impl):");
                    String pluginPackage = bufferedReader.readLine();
                    System.out.println("Enter plugin class-name (HelloPlugin):");
                    String pluginClassName = bufferedReader.readLine();

                    loadPluginFromJar(jarFilePath, pluginPackage, pluginClassName);
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

    private void loadPluginFromJar(String jarFilePath, String pluginPackage, String pluginClassName) {
        try {
            // Создаем загрузчик
            DynamicPluginLoader dynamicPluginLoader = new DynamicPluginLoader(jarFilePath, pluginPackage);
            // Загружаем класс
            Class clazz = Class.forName(pluginPackage + PACKAGE_DELIMITER + pluginClassName, true, dynamicPluginLoader);
            // Создаем экземпляр класса
            log.info("Class " + clazz.getName() + " loaded by " + clazz.getClassLoader());
            Plugin plugin = (Plugin) clazz.newInstance();
            plugin.run();
        } catch (ClassNotFoundException e) {
            log.error(pluginClassName + " plugin not found in jar.", e);
        } catch (InstantiationException e) {
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error(e);
        }
    }
}
