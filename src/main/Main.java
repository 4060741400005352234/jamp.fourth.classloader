package main;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        PluginService pluginService = new PluginService();
        pluginService.performLoadPlugin();
    }
}
