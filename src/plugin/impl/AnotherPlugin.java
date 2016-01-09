package plugin.impl;

import plugin.Plugin;

public class AnotherPlugin implements Plugin {

    @Override
    public void run() {
        System.out.println("Hello from another plugin!");
    }
}
