package io.prometheus.client.exporter;

import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TestHTTPServerBasicAuthenticator {

    @Test
    public void testUsernameAndPassword() {
        HTTPServerBasicAuthenticator authenticator = new HTTPServerBasicAuthenticator("test", "secret");
        Assert.assertTrue(authenticator.checkCredentials("test", "secret"));
    }

    @Test
    public void testWrongUsername() {
        HTTPServerBasicAuthenticator authenticator = new HTTPServerBasicAuthenticator("test", "secret");

        Assert.assertFalse(authenticator.checkCredentials("wrong", "secret"));
    }

    @Test
    public void testWrongPassword() {
        HTTPServerBasicAuthenticator authenticator = new HTTPServerBasicAuthenticator("test", "secret");
        Assert.assertFalse(authenticator.checkCredentials("test", "wrong"));
    }

    @Test
    public void testNullUsername() {
        try {
            new HTTPServerBasicAuthenticator(null, "secret");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage().contains("username is null or empty"));
        }
    }

    @Test
    public void testEmptyUsername() {
        try {
            new HTTPServerBasicAuthenticator("", "secret");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage().contains("username is null or empty"));
        }
    }

    @Test
    public void testNullPassword() {
        try {
            new HTTPServerBasicAuthenticator("test", null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage().contains("password is null or empty"));
        }
    }

    @Test
    public void testEmptyPassword() {
        try {
            new HTTPServerBasicAuthenticator("test", "");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage().contains("password is null or empty"));
        }
    }
}
