package com.letuc.tester.reader;

import java.nio.file.Files;
import java.nio.file.Path;

public class TemplateLoader {

    private final Path templateDirectory;

    public TemplateLoader(Path templateDirectory) {
        this.templateDirectory = templateDirectory;
        if (!Files.exists(templateDirectory)) {
            System.err.println("警告: 接口文件不存在: " + templateDirectory.toAbsolutePath());
        }
    }

//    public String loadTemplate(ApiDefinition apiDef, ControllerInfo controllerInfo) {
//        String methodName = getSimpleMethodName(controllerInfo.methodName);
//        String className = apiDef.className;
//
//        String templateFileName = className + "." + methodName + ".json";
//        Path templatePath = templateDirectory.resolve(templateFileName);
//
//        if (!Files.exists(templatePath)) {
//            System.out.println("提示: 未找到方法 " + methodName + " 的文件, 路径: " + templatePath.toAbsolutePath());
//            return null;
//        }
//
//        try {
//            return Files.readString(templatePath, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            System.err.println("错误: 无法读取接口文件: " + templatePath.toAbsolutePath());
//            return "{\"error\": \"Failed to load template file: " + e.getMessage() + "\"}";
//        }
//    }
//
//    private String getSimpleMethodName(String fullSignature) {
//        try {
//            String partBeforeParen = fullSignature.split("\\(")[0];
//            String[] parts = partBeforeParen.split("\\.");
//            return parts[parts.length - 1];
//        } catch (Exception e) {
//            return fullSignature;
//        }
//    }
}