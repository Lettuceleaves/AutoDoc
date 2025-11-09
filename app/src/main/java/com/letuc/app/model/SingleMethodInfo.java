package com.letuc.app.model;

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
    private Path filePath;
    private String httpMethod;
    private String url;
    private String signature;
    List<InputParam> inputParams;
    OutputParam outputParam;
    private MethodDeclaration methodNode;
}