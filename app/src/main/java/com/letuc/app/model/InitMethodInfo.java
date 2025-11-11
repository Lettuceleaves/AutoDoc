package com.letuc.app.model;

import lombok.Data;

import java.util.List;

@Data
public class InitMethodInfo {

    private final String describe;
    private final String className;
    private final String methodName;
    private final List<String> paramTypes;
    private final boolean isConstructor;

    public InitMethodInfo(String className, String methodName, List<String> paramTypes, boolean isConstructor) {
        this.className = className;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.isConstructor = isConstructor;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(className).append(".").append(methodName).append("(");
        for (int i = 0; i < paramTypes.size() - 1; i++) {
            stringBuilder.append(paramTypes.get(i));
            stringBuilder.append(", ");
        }
        stringBuilder.append(paramTypes.get(paramTypes.size() - 1));
        stringBuilder.append(")");
        this.describe = stringBuilder.toString();
    }
}