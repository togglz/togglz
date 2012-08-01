package org.togglz.spring.test.proxy;

public class SomeServiceInactive implements SomeService {

    @Override
    public String whoAreYou() {
        return "I'm SomeServiceInactive";
    }

}
