package com.letuc.app.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.model.SingleMethodInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParseSingleController {

    private static final List<String> CONTROLLER_ANNOTATIONS =
            List.of("Controller", "RestController");

    public static SingleControllerInfo parse(Path file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            Optional<ClassOrInterfaceDeclaration> controllerClass = findControllerClass(cu);

            if (controllerClass.isEmpty()) {
                System.err.println("错误: 文件中未找到 Controller 类注解。");
                return null;
            }

            ClassOrInterfaceDeclaration controller = controllerClass.get();
            SingleControllerInfo controllerInfo = new SingleControllerInfo();
            controllerInfo.setClassName(controller.getNameAsString());

            printMethods(controller);

            List<SingleMethodInfo> methodInfos = controller.getMethods().stream()
                    .map(method -> ParseSingleMethod.parse(method, file))
                    .toList();

            methodInfos = ParseParams.parse(methodInfos);

            return null;
        } catch (Exception e) {
            System.err.println("错误: 解析 AST 或查找 Controller 时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Optional<ClassOrInterfaceDeclaration> findControllerClass(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(classDeclaration ->
                        classDeclaration.isPublic() &&
                                classDeclaration.getAnnotations().stream()
                                        .anyMatch(annotation ->
                                                CONTROLLER_ANNOTATIONS.contains(annotation.getNameAsString())
                                        )
                )
                .findFirst();
    }

    private static void printMethods(ClassOrInterfaceDeclaration controller) {
        System.out.println("""
                
                ---------- 收集到的方法列表 ----------
                """);
        List<MethodDeclaration> methods = controller.getMethods();
        for (MethodDeclaration method : methods) {
            System.out.print(method.getTypeAsString() + " " + method.getNameAsString() + "(");
            String parametersString = method.getParameters().stream()
                    .map(p -> p.getTypeAsString() + " " + p.getNameAsString())
                    .collect(Collectors.joining(", "));

            System.out.println(parametersString + ")");

        }

        System.out.println("\n-------------------------------------------");
        System.out.println("总共找到方法: " + methods.size() + " 个");
        System.out.println("-------------------------------------------");
    }

}
