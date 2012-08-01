package org.togglz.spring.test.proxy;

public class SomeServiceActive implements SomeService {

    @Override
    public String whoAreYou() {
        return "I'm SomeServiceActive";
    }

}
