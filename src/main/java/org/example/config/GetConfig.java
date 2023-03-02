package org.example.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class GetConfig {
    private static GetConfig pp;
    private static Properties properties = new Properties();

    public static GetConfig getInstance(String fileName) {
        if (pp == null) {
            pp = new GetConfig();
            try {
                File file1 = new File(fileName);
                File file2 = new File(System.getProperty("user.dir") + File.separator + fileName);
                if (file1.exists()) properties.load(Files.newInputStream(Paths.get(fileName)));
                else if (file2.exists()) properties.load(Files.newInputStream(file2.toPath()));
                else properties.load(GetConfig.class.getClassLoader().getResourceAsStream(fileName));
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        return pp;
    }

    public String getKey(String key) {
        return properties.getProperty(key);
    }
}
