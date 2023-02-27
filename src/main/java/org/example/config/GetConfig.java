package org.example.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class GetConfig {
    private static GetConfig pp;
    private static Properties properties = new Properties();

    public static GetConfig getInstance(String fileName) {
        if (pp == null) {
            pp = new GetConfig();
            try {
                File file = new File(System.getProperty("user.dir") + File.separator + fileName);
                if (file.exists()) properties.load(new FileInputStream(file));
                else properties.load(GetConfig.class.getClassLoader().getResourceAsStream(fileName));
            } catch (Exception ignored) {
            }
        }
        return pp;
    }

    public String getKey(String key) {
        return properties.getProperty(key);
    }
}
