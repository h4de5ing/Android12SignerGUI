package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PP {
    private static PP pp;
    private static String fileName = "config.properties";
    private static Properties properties = new Properties();

    public static PP getInstance() {
        if (pp == null) {
            pp = new PP();
            try {
                File file = new File(System.getProperty("user.dir") + File.separator + fileName);
                if (file.exists()) properties.load(new FileInputStream(file));
                else properties.load(PP.class.getClassLoader().getResourceAsStream(fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pp;
    }

    public String getKey(String key) {
        return properties.getProperty(key);
    }
}
