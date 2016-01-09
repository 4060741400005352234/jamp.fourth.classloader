package main;

import loader.JarClassLoader;
import plugin.Plugin;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // Создаем загрузчик
        JarClassLoader jarClassLoader = new JarClassLoader("d:/plugins/hello_plugin.jar", "plugin.impl");
        // Загружаем класс
        Class<?> clazz = jarClassLoader.loadClass("plugin.impl.HelloPlugin");
        //Class<?> clazz= Class.forName("plugin.impl.HelloPlugin", true, jarClassLoader);
        // Создаем экземпляр класса
        Plugin sample = (Plugin) clazz.newInstance();
        sample.run();
    }
}
