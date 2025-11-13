package com.letuc.tester.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MethodInfo {
    public String httpMethod;
    public String url;
    public String signature;
    public JsonNode inputParams;
    public JsonNode outputParam;
}