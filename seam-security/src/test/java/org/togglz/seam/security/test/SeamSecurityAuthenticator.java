package org.togglz.seam.security.test;

import javax.inject.Inject;

import org.jboss.seam.security.Authenticator;
import org.jboss.seam.security.BaseAuthenticator;
import org.jboss.seam.security.Credentials;
import org.picketlink.idm.impl.api.PasswordCredential;
import org.picketlink.idm.impl.api.model.SimpleUser;

public class SeamSecurityAuthenticator extends BaseAuthenticator implements Authenticator {

    @Inject
    private Credentials credentials;

    @Override
    public void authenticate() {
        if (credentials.getCredential() instanceof PasswordCredential
                && ((PasswordCredential) credentials.getCredential()).getValue().equals("secret")) {
            this.setStatus(AuthenticationStatus.SUCCESS);
            this.setUser(new SimpleUser(credentials.getUsername()));
        }
    }

}
