package plugin.impl;

import plugin.Plugin;

public class HelloPlugin implements Plugin {

    @Override
    public void run() {
        System.out.println("Hello world from plugin!");
    }
}
