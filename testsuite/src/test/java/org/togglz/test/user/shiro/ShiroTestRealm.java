package org.togglz.test.user.shiro;

import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.realm.SimpleAccountRealm;

public class ShiroTestRealm extends SimpleAccountRealm {

    public ShiroTestRealm() {

        // a feature admin
        SimpleAccount ck = new SimpleAccount("ck", "secret", getName());
        ck.addRole("togglz");
        add(ck);

        // some other user
        SimpleAccount somebody = new SimpleAccount("somebody", "secret", getName());
        add(somebody);

    }

}
