package net.chamosmp.aBXVelocity.velocity;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class YamlConfig {
    private final Map<String, Object> data;

    private YamlConfig(Map<String, Object> data) {
        this.data = data;
    }

    public static YamlConfig load(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            Yaml yaml = new Yaml();
            Object obj = yaml.load(in);
            if (obj instanceof Map) {
                //noinspection unchecked
                return new YamlConfig((Map<String, Object>) obj);
            }
            return new YamlConfig(new LinkedHashMap<>());
        }
    }

    public Object get(String path) {
        if (path == null || path.isEmpty()) {
            return data;
        }
        String[] parts = path.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            //noinspection unchecked
            current = ((Map<String, Object>) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public Set<String> getKeys(String path) {
        Object section = get(path);
        if (section instanceof Map) {
            //noinspection unchecked
            return ((Map<String, Object>) section).keySet();
        }
        return Collections.emptySet();
    }

    public String getString(String path) {
        Object value = get(path);
        return value != null ? String.valueOf(value) : null;
    }

    public Boolean getBoolean(String path) {
        Object value = get(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    public Long getLong(String path) {
        Object value = get(path);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public Integer getInteger(String path) {
        Object value = get(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = get(path);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }
}
