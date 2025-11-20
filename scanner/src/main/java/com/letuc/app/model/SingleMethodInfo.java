package com.letuc.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleMethodInfo {

    @JsonIgnore
    private Path filePath;
    private String httpMethod;
    private String url;
    private String signature;
    private List<InputParam> inputParams;
    private OutputParam outputParam;

    @JsonIgnore
    private MethodDeclaration methodNode;
}