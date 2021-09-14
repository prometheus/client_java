package io.prometheus.client.exporter;

/**
 * Basic HTTP authenticator
 */
public class HTTPServerBasicAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {

    private static final String REALM = "/";

    private String username;
    private String password;

    public HTTPServerBasicAuthenticator(String username, String password) {
        super(REALM);

        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("username is null or empty");
        }

        if (password == null || password.trim().length() == 0) {
            throw new IllegalArgumentException("password is null or empty");
        }

        this.username = username;
        this.password = password;
    }

    /**
     * Check username and password
     * @param username
     * @param password
     * @return
     */
    @Override
    public boolean checkCredentials(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
