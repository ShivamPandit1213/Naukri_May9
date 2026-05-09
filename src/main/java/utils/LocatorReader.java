package utils;

import java.io.InputStream;
import java.util.Properties;

public class LocatorReader {

	private static Properties properties;

	private static void loadLocator() {
		if (properties == null) {
			properties = new Properties();
			try {
				InputStream is = LocatorReader.class.getClassLoader().getResourceAsStream("locator.properties");

				if (is == null) {
					throw new RuntimeException("❌ locator.properties not found in classpath");
				}

				properties.load(is);

			} catch (Exception e) {
				throw new RuntimeException("❌ Failed to load locator.properties", e);
			}
		}
	}

	public static String getLocator(String key) {
		loadLocator();
		String value = properties.getProperty(key);

		if (value == null) {
			throw new RuntimeException("❌ Locator '" + key + "' not found.");
		}

		return value;
	}
}