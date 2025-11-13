package com.letuc.tester.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ControllerInfo {

    public String methodName;
    public MethodInfo methodInfo;
}