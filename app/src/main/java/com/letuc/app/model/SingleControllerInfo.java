package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleControllerInfo {
    String className;
    Map<String, SingleMethodInfo> controllerMap;
}
