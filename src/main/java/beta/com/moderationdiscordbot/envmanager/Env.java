package beta.com.moderationdiscordbot.envmanager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Env {
    private Properties properties;

    public Env(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}