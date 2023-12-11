package io.prometheus.client.it.common;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility to get the project version, like <tt>0.12.1-SNAPSHOT</tt>
 */
public class Version {

    public static String loadProjectVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(Volume.class.getResourceAsStream("/project_version.properties"));
        return properties.getProperty("project.version");
    }
}
