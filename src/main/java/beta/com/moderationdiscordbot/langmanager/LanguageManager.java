package beta.com.moderationdiscordbot.langmanager;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class LanguageManager {
    private final Map<String, Map<String, Object>> languages = new HashMap<>();

    public LanguageManager() {
        Yaml yaml = new Yaml();
        try (Stream<Path> paths = Files.walk(Paths.get(getClass().getResource("/").toURI()))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .forEach(path -> {
                        try (InputStream in = Files.newInputStream(path)) {
                            String languageName = path.getFileName().toString().replace(".yml", "");
                            languages.put(languageName, yaml.load(in));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public String getMessage(String key, String language) {
        Map<String, Object> languageMap = languages.get(language);
        if (languageMap != null) {
            String[] keys = key.split("\\.");
            Object value = languageMap;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<String, Object>) value).get(k);
                } else {
                    return "Invalid key structure";
                }
            }
            return value != null ? value.toString() : "Key not found";
        }
        return "Unknown language";
    }
}