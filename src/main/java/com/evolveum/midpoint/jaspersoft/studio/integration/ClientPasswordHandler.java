package com.evolveum.midpoint.jaspersoft.studio.integration;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class ClientPasswordHandler implements CallbackHandler {

    private static String password;

    public static void setPassword(String password) {
        ClientPasswordHandler.password = password;
    }

    @Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        // set the password for our message.
        pc.setPassword(password != null ? password : "5ecr3t");
	}


}
