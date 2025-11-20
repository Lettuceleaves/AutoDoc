package com.letuc.app.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSON {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void saveToFile(String jsonContent, Path fullFilePath) throws IOException {
        if (jsonContent == null) {
            throw new IllegalArgumentException("JSON content cannot be null.");
        }
        if (fullFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null.");
        }
        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        Path parentDir = fullFilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        String prettyJson = objectMapper.writeValueAsString(jsonNode);

        Files.writeString(fullFilePath, prettyJson, StandardCharsets.UTF_8);
    }
}