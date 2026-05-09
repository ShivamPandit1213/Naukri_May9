package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    // Private constructor to prevent instantiation
   // private ConfigReader() { }
    // Loads properties only once
    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
                properties.load(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("❌ Failed to load config.properties file. Check the path and file.");
            }
        }
    }

    /**
     * Retrieves the property value for a given key.
     * @param key The property key
     * @return The property value as a String
     */
    public static String getConfig(String key) {
        loadProperties();
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("❌ Property '" + key + "' not found in config.properties.");
        }
        return value;
    }
}
