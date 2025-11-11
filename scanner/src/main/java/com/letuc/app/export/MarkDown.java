package com.letuc.app.export;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MarkDown {

    public static void saveToFile(String content, String fullFilePath) throws Exception {
        content = convert(content);
        if (content == null) {
            throw new IllegalArgumentException("File content cannot be null.");
        }
        if (fullFilePath == null || fullFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        Path path = Paths.get(fullFilePath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }


    private static final String INDENT = "  ";

    private static class ManualParser {
        private final String json;
        private int index;

        ManualParser(String jsonString) {
            this.json = jsonString;
            this.index = 0;
        }

        void buildMd(StringBuilder mdBuilder) {
            buildMdRecursive(0, mdBuilder);
        }

        private void buildMdRecursive(int indentLevel, StringBuilder mdBuilder) {
            skipWhitespace();
            if (index >= json.length()) return;

            char c = json.charAt(index);

            if (c == '{') {
                buildObject(indentLevel, mdBuilder);
            } else if (c == '[') {
                buildArray(indentLevel, mdBuilder);
            } else {
                mdBuilder.append(parsePrimitive()).append("\n");
            }
        }

        private void buildObject(int indentLevel, StringBuilder mdBuilder) {
            index++;
            String indent = INDENT.repeat(indentLevel);

            while (true) {
                skipWhitespace();
                if (index >= json.length()) return;

                if (json.charAt(index) == '}') {
                    index++;
                    return;
                }

                String keyWithQuotes = parseString();
                String key = keyWithQuotes.substring(2, keyWithQuotes.length() - 2);
                mdBuilder.append(indent).append("- `").append(key).append("`:");

                skipWhitespace();
                if (index >= json.length() || json.charAt(index) != ':') return;
                index++;
                skipWhitespace();
                if (index >= json.length()) return;

                char c = json.charAt(index);
                if (c == '{' || c == '[') {
                    mdBuilder.append("\n");
                    buildMdRecursive(indentLevel + 1, mdBuilder);
                } else {
                    mdBuilder.append(" ").append(parsePrimitive()).append("\n");
                }

                skipWhitespace();
                if (index >= json.length()) return;

                if (json.charAt(index) == '}') {
                    index++;
                    return;
                }

                if (json.charAt(index) != ',') return;
                index++;
            }
        }

        private void buildArray(int indentLevel, StringBuilder mdBuilder) {
            index++;
            String indent = INDENT.repeat(indentLevel);

            while (true) {
                skipWhitespace();
                if (index >= json.length()) return;

                if (json.charAt(index) == ']') {
                    index++;
                    return;
                }

                mdBuilder.append(indent).append("1. ");

                skipWhitespace();
                if (index >= json.length()) return;

                char c = json.charAt(index);
                if (c == '{' || c == '[') {
                    mdBuilder.append("\n");
                    buildMdRecursive(indentLevel + 1, mdBuilder);
                } else {
                    mdBuilder.append(parsePrimitive()).append("\n");
                }

                skipWhitespace();
                if (index >= json.length()) return;

                if (json.charAt(index) == ']') {
                    index++;
                    return;
                }

                if (json.charAt(index) != ',') return;
                index++;
            }
        }

        private String parsePrimitive() {
            skipWhitespace();
            if (index >= json.length()) return "`ERROR`";

            char c = json.charAt(index);
            if (c == '"') {
                return parseString();
            }
            if (c == 't' || c == 'f' || c == 'n') {
                return parseLiteral();
            }
            if ((c >= '0' && c <= '9') || c == '-') {
                return parseNumber();
            }
            return "`INVALID_PRIMITIVE`";
        }

        private String parseString() {
            int start = index;
            index++;

            while (index < json.length()) {
                char c = json.charAt(index);
                if (c == '\\') {
                    index += 2;
                } else if (c == '"') {
                    break;
                } else {
                    index++;
                }
            }
            index++;

            String value = json.substring(start, index);
            return "`" + value + "`";
        }

        private String parseNumber() {
            int start = index;
            while (index < json.length()) {
                char c = json.charAt(index);
                if ((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                    index++;
                } else {
                    break;
                }
            }
            String value = json.substring(start, index);
            return "`" + value + "`";
        }

        private String parseLiteral() {
            if (json.startsWith("true", index)) {
                index += 4;
                return "`true`";
            }
            if (json.startsWith("false", index)) {
                index += 5;
                return "`false`";
            }
            if (json.startsWith("null", index)) {
                index += 4;
                return "`null`";
            }
            return "`INVALID_LITERAL`";
        }

        private void skipWhitespace() {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
        }
    }

    public static String convert(String jsonString) throws Exception {
        if (jsonString == null || jsonString.isEmpty() || jsonString.trim().isEmpty()) {
            return "";
        }

        StringBuilder mdBuilder = new StringBuilder();
        mdBuilder.append("**JSON Structure (Markdown):**\n\n");

        try {
            ManualParser parser = new ManualParser(jsonString);
            parser.buildMd(mdBuilder);
        } catch (Exception e) {
            return "Error parsing JSON (malformed input): " + e.getMessage();
        }

        return mdBuilder.toString();
    }
}