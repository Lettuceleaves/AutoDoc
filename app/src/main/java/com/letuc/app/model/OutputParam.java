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
public class OutputParam {
    String type;
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
                .map(OutputParam::getType)
                .collect(Collectors.toList());

        String className = this.type;
        String methodName = this.type;

        if (this.type.contains(".")) {
            methodName = this.type.substring(this.type.lastIndexOf('.') + 1);
        }

        InitMethodInfo allArgsMethod = new InitMethodInfo(
                className,
                methodName,
                paramTypes,
                true
        );
        this.methods.add(allArgsMethod);
        this.methodsFilter.add(allArgsMethod.getDescribe());
    }
}
