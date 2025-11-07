package com.letuc.app.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.model.SingleMethodInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParseSingleController {

    private static final List<String> CONTROLLER_ANNOTATIONS =
            List.of("Controller", "RestController");

    private static final List<String> CLASS_MAPPING_ANNOTATIONS =
            List.of("RequestMapping");

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

            String classPath = resolveClassPath(controller);
            controllerInfo.setUrl(classPath);

            printMethods(controller);

            List<SingleMethodInfo> methodInfos = controller.getMethods().stream()
                    .map(method -> ParseSingleMethod.parse(method, file))
                    .toList();

            ParseInputParams.parse(methodInfos);
            ParseOutputParam.parse(methodInfos);

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

    private static String resolveClassPath(ClassOrInterfaceDeclaration controller) {
        Optional<AnnotationExpr> mappingAnnotationOpt = controller.getAnnotations().stream()
                .filter(a -> CLASS_MAPPING_ANNOTATIONS.contains(a.getNameAsString()))
                .findFirst();

        if (mappingAnnotationOpt.isEmpty()) {
            return "";
        }

        AnnotationExpr annotation = mappingAnnotationOpt.get();

        if (annotation.isSingleMemberAnnotationExpr()) {
            Expression value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
            return extractStringValue(value).orElse("");
        }
        else if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();

            Optional<Expression> pathExpression = normalAnnotation.getPairs().stream()
                    .filter(pair -> pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path"))
                    .map(MemberValuePair::getValue)
                    .findFirst();

            return pathExpression.flatMap(ParseSingleController::extractStringValue).orElse("");
        }

        return "";
    }

    private static Optional<String> extractStringValue(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            return Optional.of(expression.asStringLiteralExpr().getValue());
        }
        if (expression.isArrayInitializerExpr()) {
            NodeList<Expression> values = expression.asArrayInitializerExpr().getValues();
            if (!values.isEmpty()) {
                return extractStringValue(values.get(0));
            }
        }
        return Optional.empty();
    }

}
