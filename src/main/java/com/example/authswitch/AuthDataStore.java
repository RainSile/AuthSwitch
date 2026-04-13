package com.example.authswitch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class AuthDataStore {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path storePath;

    public AuthDataStore() {
        this.storePath = Paths.get(System.getProperty("user.home"), ".codex", "auth-switch-schemes.json");
    }

    public AuthData load() {
        if (!Files.exists(storePath)) {
            return new AuthData();
        }

        try {
            AuthData data = objectMapper.readValue(storePath.toFile(), AuthData.class);
            if (data.getSchemes() == null) {
                data.setSchemes(new java.util.ArrayList<>());
            }
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("无法读取方案文件: " + storePath, e);
        }
    }

    public void save(AuthData data) {
        try {
            Files.createDirectories(storePath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile(), data);
        } catch (IOException e) {
            throw new IllegalStateException("无法保存方案文件: " + storePath, e);
        }
    }

    public void writeCurrentAuthFile(String apiKey) {
        Path authFilePath = Paths.get(System.getProperty("user.home"), ".codex", "auth.json");

        try {
            Files.createDirectories(authFilePath.getParent());
            Map<String, Object> authJson;
            if (Files.exists(authFilePath)) {
                authJson = objectMapper.readValue(authFilePath.toFile(), new TypeReference<Map<String, Object>>() {
                });
            } else {
                authJson = new java.util.LinkedHashMap<>();
            }
            authJson.put("OPENAI_API_KEY", apiKey);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(authFilePath.toFile(), authJson);
        } catch (IOException e) {
            throw new IllegalStateException("无法写入 auth 文件: " + authFilePath, e);
        }
    }

    public Path getStorePath() {
        return storePath;
    }
}
