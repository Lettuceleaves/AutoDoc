package com.letuc.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputParam {
    String type;
    String name;
    List<OutputParam> subParams;
    List<InitMethodInfo> methods;

    public void addAllArgsConstructorToInitMethods() {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }
        if (this.subParams == null) {
            this.subParams = new ArrayList<>();
        }
        List<String> paramTypes = this.subParams.stream()
                .map(OutputParam::getType)
                .collect(Collectors.toList());
        String descriptor = this.type +
                "<init>(" +
                String.join(",", paramTypes) +
                ")";
        InitMethodInfo allArgsMethod = new InitMethodInfo(
                descriptor,
                this.type,
                "<init>",
                paramTypes,
                true
        );
        this.methods.add(allArgsMethod);
    }

}
