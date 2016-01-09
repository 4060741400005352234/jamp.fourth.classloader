package main;

import loader.JarClassLoader;
import plugin.Plugin;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // Создаем загрузчик
        JarClassLoader jarClassLoader = new JarClassLoader("d:/plugins/hello_plugin.jar", "plugin.impl");
        // Загружаем класс
        Class<?> clazz = jarClassLoader.loadClass("plugin.impl.HelloPlugin");
        System.out.println("== " + clazz.getClassLoader());
        Class<?> clazz2= Class.forName("plugin.impl.AnotherPlugin", true, jarClassLoader);
        System.out.println("== " + clazz2.getClassLoader());
        // Создаем экземпляр класса
        Plugin sample = (Plugin) clazz.newInstance();
        Plugin sample2 = (Plugin) clazz2.newInstance();
        sample.run();
        sample2.run();
    }
}
