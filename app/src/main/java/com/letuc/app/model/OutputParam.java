package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputParam { // TODO 分离出参根和子出参
    String className;
    String origin;
    String name;
    List<OutputParam> subParams;
    Set<InitMethodInfo> methods;
    Set<String> methodsFilter;

    public void addAllArgsConstructorToInitMethods() {
        if (this.methods == null) {
            this.methods = new HashSet<>();
        }
        if (this.methodsFilter == null) {
            this.methodsFilter = new HashSet<>();
        }
        if (this.subParams == null) {
            this.subParams = new ArrayList<>();
        }

        List<String> paramTypes = this.subParams.stream()
                .map(OutputParam::getClassName)
                .collect(Collectors.toList());

        String className = this.className;
        String originName = this.className;

        if (this.className.contains(".")) {
            originName = this.className.substring(this.className.lastIndexOf('.') + 1);
        }

        InitMethodInfo allArgsMethod = new InitMethodInfo(
                className,
                originName,
                paramTypes,
                true
        );
        this.methods.add(allArgsMethod);
        this.methodsFilter.add(allArgsMethod.getDescribe());
    }

    public String toJsonContent() {
        StringBuilder sb = new StringBuilder();

        sb.append("\"className\":\"").append(className).append("\",");
        if (origin != null) {
            sb.append("\"origin\":\"").append(origin).append("\",");
        }
        sb.append("\"name\":\"").append(name).append("\"");
        if (this.subParams != null && !this.subParams.isEmpty()) {
            sb.append(",\"subParams\":[");
            for (int i = 0; i < subParams.size(); i++) {
                sb.append(subParams.get(i).toJson());
                if (i < subParams.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public String toJson() {
        return "{" + toJsonContent() + "}";
    }
}
