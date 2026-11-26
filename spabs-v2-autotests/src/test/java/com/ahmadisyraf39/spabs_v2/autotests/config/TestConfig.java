package com.ahmadisyraf39.spabs_v2.autotests.config;

public final class TestConfig {

    public static final String BASE_URL = "http://localhost:5173";
    public static final String API_BASE_URL = "http://localhost:8080/api/v1";
    public static final boolean HEADLESS = Boolean.parseBoolean(setting("HEADLESS", "false"));
    public static final String MAILPIT_URL = setting("MAILPIT_URL", "http://localhost:8025");

    private TestConfig() {}

    // Order: -DNAME=value on the mvn command line, then the NAME env var, then the default
    private static String setting(String name, String defaultValue) {
        String fromSystemProperty = System.getProperty(name);
        if (fromSystemProperty != null) {
            return fromSystemProperty;
        }
        String fromEnv = System.getenv(name);
        return fromEnv != null ? fromEnv : defaultValue;
    }

}
