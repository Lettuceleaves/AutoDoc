package com.letuc.tester.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ApiDefinition {

    public String className;
    public String url;
    public List<ControllerInfo> controllerMap;
}