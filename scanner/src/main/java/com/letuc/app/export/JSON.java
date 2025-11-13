package com.letuc.app.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSON {
    public static void saveToFile(String jsonContent, Path fullFilePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonContent);
        jsonContent = gson.toJson(jsonElement);
        if (jsonContent == null) {
            throw new IllegalArgumentException("JSON content cannot be null.");
        }
        if (fullFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        Path parentDir = fullFilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(fullFilePath, jsonContent, StandardCharsets.UTF_8);
    }
}