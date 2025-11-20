package com.letuc.app.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MarkDown {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    public static void saveToFile(String content, Path fullFilePath) throws Exception {
        String mdContent = convert(content);

        if (fullFilePath == null) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        Path parentDir = fullFilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(fullFilePath, mdContent, StandardCharsets.UTF_8);
    }

    public static String convert(String jsonString) throws Exception {
        if (jsonString == null || jsonString.isEmpty() || jsonString.trim().isEmpty()) {
            return "";
        }

        try {
            List<ApiDoc> apiDocList = objectMapper.readValue(jsonString, new TypeReference<List<ApiDoc>>() {});

            StringBuilder fullMarkdown = new StringBuilder();

            for (ApiDoc apiDoc : apiDocList) {
                fullMarkdown.append(buildMarkdownFromApiDoc(apiDoc));
                fullMarkdown.append("\n\n<br>\n\n---\n\n<br>\n\n");
            }

            return fullMarkdown.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing JSON (malformed input): " + e.getMessage();
        }
    }

    private static String buildMarkdownFromApiDoc(ApiDoc apiDoc) {
        StringBuilder md = new StringBuilder();

        md.append("# 接口文档: ").append(apiDoc.className).append("\n\n");
        md.append("**基础路径 (Base URL):** `").append(apiDoc.url).append("`\n\n");
        md.append("---\n\n");

        if (apiDoc.controllerMap == null || apiDoc.controllerMap.isEmpty()) {
            md.append("*此控制器下没有定义接口。*\n");
            return md.toString();
        }

        int index = 1;
        for (ControllerInfo controller : apiDoc.controllerMap) {
            MethodInfo method = controller.methodInfo;
            if (method == null) continue;

            md.append("## ").append(index++).append(". ")
                    .append(getFriendlyMethodName(controller.methodName))
                    .append(" (`").append(method.httpMethod).append(" ")
                    .append(apiDoc.url).append(method.url).append("`)\n\n");

            md.append("**完整方法签名:** `").append(method.signature).append("`\n\n");

            buildInputSection(md, method.inputParams);

            buildOutputSection(md, method.outputParam);

            md.append("---\n\n");
        }

        return md.toString();
    }

    private static void buildInputSection(StringBuilder md, List<InputParam> params) {
        md.append("### 请求参数 (Request)\n\n");
        if (params == null || params.isEmpty()) {
            md.append("*无请求参数。*\n\n");
            return;
        }

        md.append("| 参数名 | 类型 | 位置 | 描述 |\n");
        md.append("| :--- | :--- | :--- | :--- |\n");

        InputParam bodyParam = null;
        for (InputParam p : params) {
            String location = p.field != null ? p.field : "UNKNOWN";
            md.append("| `").append(p.name).append("` | `")
                    .append(formatType(p.type)).append("` | `")
                    .append(location).append("` | |\n");

            if ("BODY".equalsIgnoreCase(p.field) && p.subParams != null && !p.subParams.isEmpty()) {
                bodyParam = p;
                for (InputSubParam sub : p.subParams) {
                    md.append("| &nbsp;&nbsp;&nbsp;↳ `").append(sub.name).append("` | `")
                            .append(formatType(sub.type)).append("` | `BODY` | |\n");
                }
            }
        }
        md.append("\n");

        if (bodyParam != null) {
            md.append("#### 请求体示例 (Request Body Example)\n\n");
            md.append("```json\n");
            md.append(generateRequestJsonExample(bodyParam));
            md.append("\n```\n\n");
        }
    }

    private static void buildOutputSection(StringBuilder md, OutputParam output) {
        md.append("### 响应内容 (Response)\n\n");
        if (output == null || output.subParams == null || output.subParams.isEmpty()) {
            md.append("*无响应体。*\n\n");
            return;
        }

        md.append("**响应类型:** `").append(formatType(output.origin != null ? output.origin : output.className)).append("`\n\n");

        md.append("| 字段名 | 类型 | 描述 |\n");
        md.append("| :--- | :--- | :--- |\n");
        buildOutputTableRecursive(md, output.subParams, 0);
        md.append("\n");

        buildEnumValuesRecursive(md, output.subParams);

        md.append("#### 响应体示例 (Response Body Example)\n\n");
        md.append("```json\n");
        md.append(generateResponseJsonExample(output));
        md.append("\n```\n\n");
    }

    private static void buildOutputTableRecursive(StringBuilder md, List<OutputSubParam> params, int indentLevel) {
        if (params == null) return;
        String indent = "&nbsp;&nbsp;&nbsp;".repeat(indentLevel) + (indentLevel > 0 ? "↳ " : "");

        for (OutputSubParam p : params) {
            String fieldName = getValidName(p);

            md.append("| ").append(indent).append("`").append(fieldName).append("` | `")
                    .append(formatType(p.className)).append("` | ").append(" |\n");

            buildOutputTableRecursive(md, p.subParams, indentLevel + 1);
        }
    }

    /**
     * 【修改点 1】 适配新的 JSON 结构，直接读取 description 字段
     */
    private static void buildEnumValuesRecursive(StringBuilder md, List<OutputSubParam> params) {
        if (params == null) return;

        for (OutputSubParam p : params) {
            if (p.values != null && !p.values.isEmpty()) {
                String fieldName = getValidName(p);

                md.append("**字段 `").append(fieldName).append("` 的可选值:**\n\n");

                md.append("| 值 (Value) | 描述 (Description) |\n");
                md.append("| :--- | :--- |\n");

                for (JsonNode valNode : p.values) {
                    String valueStr = "";
                    String descStr = "";

                    // 1. 获取 Value
                    if (valNode.has("value")) {
                        // 现在的 value 字段就是纯字符串了（因为 description 已经分离出去了）
                        valueStr = valNode.get("value").asText();
                        // 美化：去除包名
                        if (valueStr.contains(".")) {
                            valueStr = valueStr.substring(valueStr.lastIndexOf('.') + 1);
                        }
                    }

                    // 2. 获取 Description (新加的逻辑)
                    if (valNode.has("description") && !valNode.get("description").isNull()) {
                        descStr = valNode.get("description").asText();
                    }

                    md.append("| `").append(valueStr).append("` | ").append(descStr).append(" |\n");
                }
                md.append("\n");
            }
            buildEnumValuesRecursive(md, p.subParams);
        }
    }

    private static String generateRequestJsonExample(InputParam bodyParam) {
        ObjectNode root = objectMapper.createObjectNode();
        if (bodyParam.subParams != null) {
            for (InputSubParam sub : bodyParam.subParams) {
                root.put(sub.name, getExampleValue(sub.type));
            }
        }
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate example\"}";
        }
    }

    private static String generateResponseJsonExample(OutputParam output) {
        ObjectNode root = objectMapper.createObjectNode();
        if (output.subParams != null) {
            for (OutputSubParam sub : output.subParams) {
                root.set(getValidName(sub), buildJsonNodeRecursive(sub));
            }
        }
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate example: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 【修改点 2】 生成 JSON 示例时，也适配一下新的结构
     */
    private static JsonNode buildJsonNodeRecursive(OutputSubParam param) {
        if (param.subParams != null && !param.subParams.isEmpty()) {
            ObjectNode node = objectMapper.createObjectNode();
            for (OutputSubParam sub : param.subParams) {
                node.set(getValidName(sub), buildJsonNodeRecursive(sub));
            }
            return node;
        }

        // 尝试从 values 列表中取第一个值作为示例
        if (param.values != null && !param.values.isEmpty()) {
            JsonNode firstVal = param.values.get(0);
            if (firstVal.has("value")) {
                String valStr = firstVal.get("value").asText();
                // 美化
                if (valStr.contains(".")) valStr = valStr.substring(valStr.lastIndexOf('.') + 1);
                return objectMapper.valueToTree(valStr);
            }
        }

        return getExampleValue(param.className);
    }

    private static String getValidName(OutputSubParam p) {
        if (p.name != null && !p.name.isEmpty() && !"null".equals(p.name)) {
            return p.name;
        }
        if (p.origin != null && !p.origin.isEmpty()) {
            return p.origin;
        }
        return "unknown_field";
    }

    private static JsonNode getExampleValue(String type) {
        type = formatType(type).toLowerCase();
        if (type.contains("string") || type.contains("char")) {
            return objectMapper.valueToTree("string");
        }
        if (type.contains("int") || type.contains("integer") || type.contains("long") || type.contains("short")) {
            return objectMapper.valueToTree(1);
        }
        if (type.contains("double") || type.contains("float")) {
            return objectMapper.valueToTree(1.0);
        }
        if (type.contains("boolean")) {
            return objectMapper.valueToTree(true);
        }
        if (type.contains("byte[]")) {
            return objectMapper.valueToTree("base64-encoded-string");
        }
        return objectMapper.valueToTree(null);
    }

    private static String formatType(String javaType) {
        if (javaType == null) return "unknown";
        return javaType.replaceAll("java\\.lang\\.", "");
    }

    private static String getFriendlyMethodName(String fullMethodName) {
        try {
            String[] parts = fullMethodName.split("\\(")[0].split("\\.");
            return parts[parts.length - 1];
        } catch (Exception e) {
            return fullMethodName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApiDoc {
        public String className;
        public String url;
        public List<ControllerInfo> controllerMap;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ControllerInfo {
        public String methodName;
        public MethodInfo methodInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MethodInfo {
        public String httpMethod;
        public String url;
        public String signature;
        public List<InputParam> inputParams;
        public OutputParam outputParam;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class InputParam {
        public String type;
        public String name;
        public String field;
        public List<InputSubParam> subParams;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class InputSubParam {
        public String type;
        public String name;
        public String field;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OutputParam {
        public String className;
        public String origin;
        public String name;
        public List<OutputSubParam> subParams;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OutputSubParam {
        public String className;
        public String origin;
        public String name;
        public List<JsonNode> values;
        public List<OutputSubParam> subParams;
    }
}