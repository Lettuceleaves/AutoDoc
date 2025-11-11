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

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"httpMethod\":\"").append(httpMethod).append("\",");
        sb.append("\"url\":\"").append(url).append("\",");
        sb.append("\"signature\":\"").append(signature).append("\",");
        sb.append("\"inputParams\":[");
        for (InputParam inputParam : inputParams) {
            sb.append(inputParam.toJson());
        }
        sb.append("],");
        sb.append("\"outputParam\":").append(outputParam.toJson());
        sb.append("}");
        return sb.toString();
    }
}