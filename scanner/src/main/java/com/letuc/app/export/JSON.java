package com.letuc.app.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSON {
    public static void saveToFile(String jsonContent, String fullFilePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonContent);
        jsonContent = gson.toJson(jsonElement);
        if (jsonContent == null) {
            throw new IllegalArgumentException("JSON content cannot be null.");
        }
        if (fullFilePath == null || fullFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        Path path = Paths.get(fullFilePath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(path, jsonContent, StandardCharsets.UTF_8);
    }
}