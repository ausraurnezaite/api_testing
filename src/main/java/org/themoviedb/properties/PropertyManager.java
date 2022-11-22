package org.themoviedb.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertyManager {
    private static final String DATA_PATH = "src/test/resources/properties/testData.properties";

    public static String getProperty(String name) {
        Properties props = new Properties();
        try {
            props.load(new InputStreamReader(new FileInputStream(DATA_PATH), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return props.getProperty(name);
    }
}
