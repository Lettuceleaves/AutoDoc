package com.letuc.tester.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.letuc.tester.model.ApiDefinition;
import com.letuc.tester.model.ControllerInfo;
import com.letuc.tester.model.MethodInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

public class TaskFactory {

    private final String baseHost;
    private final ObjectMapper mapper;

    public TaskFactory(String baseHost) {
        this.baseHost = baseHost;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<SimpleTask> createTasksFromJson(String jsonDefinitionString) {
        List<SimpleTask> tasks = new ArrayList<>();
        try {
            ApiDefinition apiDef = mapper.readValue(jsonDefinitionString, ApiDefinition.class);

            if (apiDef.controllerMap == null) {
                return tasks;
            }

            for (ControllerInfo controllerInfo : apiDef.controllerMap) {
                MethodInfo methodInfo = controllerInfo.methodInfo;

                String url = baseHost + apiDef.url + methodInfo.url;
                HttpMethod method = HttpMethod.valueOf(methodInfo.httpMethod.toUpperCase());

                HttpHeaders headers = new HttpHeaders();
                String requestBodyTemplate = null;

                JsonNode inputParamsNode = methodInfo.inputParams;
                if (inputParamsNode != null && inputParamsNode.isArray()) {
                    JsonNode bodyParamDef = findBodyParamDefinition(inputParamsNode);

                    if (bodyParamDef != null) {
                        requestBodyTemplate = generateTemplateFromSubParams(bodyParamDef.get("subParams"));
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    }
                }

                String taskName = apiDef.className + "." + getSimpleMethodName(controllerInfo.methodName);
                String outputTemplate = (methodInfo.outputParam != null) ? mapper.writeValueAsString(methodInfo.outputParam) : null;
                tasks.add(new SimpleTask(
                        taskName,
                        url,
                        method,
                        headers,
                        requestBodyTemplate,
                        outputTemplate
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private JsonNode findBodyParamDefinition(JsonNode inputParamsNode) {
        for (JsonNode paramNode : inputParamsNode) {
            if (paramNode.has("field") && "BODY".equalsIgnoreCase(paramNode.get("field").asText())) {
                return paramNode;
            }
        }
        return null;
    }

    private String generateTemplateFromSubParams(JsonNode subParamsArray) {
        if (subParamsArray == null || !subParamsArray.isArray()) {
            return null;
        }

        ObjectNode root = mapper.createObjectNode();

        for (JsonNode subParam : subParamsArray) {
            String name = subParam.get("name").asText();
            String type = subParam.get("type").asText();
            root.set(name, getExampleValue(type));
        }

        try {
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate template\"}";
        }
    }

    private JsonNode getExampleValue(String type) {
        if (type == null) return mapper.nullNode();
        String simpleType = type.toLowerCase().replaceAll("java\\.lang\\.", "");

        if (simpleType.contains("string") || simpleType.contains("char")) {
            return mapper.valueToTree("string_placeholder");
        }
        if (simpleType.contains("int") || simpleType.contains("integer") || simpleType.contains("long")) {
            return mapper.valueToTree(0);
        }
        if (simpleType.contains("boolean")) {
            return mapper.valueToTree(false);
        }
        return mapper.nullNode();
    }

    private String getSimpleMethodName(String fullSignature) {
        try {
            String partBeforeParen = fullSignature.split("\\(")[0];
            String[] parts = partBeforeParen.split("\\.");
            return parts[parts.length - 1];
        } catch (Exception e) {
            return fullSignature;
        }
    }
}