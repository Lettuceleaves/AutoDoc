package com.letuc.app.parser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.letuc.app.model.InputParam;
import com.letuc.app.model.OutputParam;
import com.letuc.app.model.SingleMethodInfo;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ParseSingleMethod {

    private static final List<String> MAPPING_ANNOTATIONS = Arrays.asList(
            "RequestMapping", "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping"
    );

    public static SingleMethodInfo parse(MethodDeclaration method, Path path) {
        Map.Entry<String, String> httpInfo = resolveHttpMethodAndPath(method);

        List<InputParam> inputParams = method.getParameters().stream()
                .map(param -> {
                    String qualifiedType;

                    try {
                        ResolvedType resolvedType = param.getType().resolve();
                        qualifiedType = resolvedType.describe();
                    } catch (Exception e) {
                        qualifiedType = param.getTypeAsString();
                    }

                    return new InputParam(qualifiedType, param.getNameAsString(), null);
                })
                .collect(Collectors.toList());

        OutputParam outputParam;
        String outputTypeFqn;
        String origin;

        try {
            ResolvedType resolvedType = method.getType().resolve();
            origin = resolvedType.describe();
            if (resolvedType.isReferenceType()) {
                outputTypeFqn = resolvedType.asReferenceType().getQualifiedName();
            } else {
                outputTypeFqn = resolvedType.describe();
            }

        } catch (Exception e) {
            outputTypeFqn = method.getTypeAsString();
            origin = method.getTypeAsString();
        }

        outputParam = new OutputParam(outputTypeFqn, origin, null, null, null, null);

        return new SingleMethodInfo(
                path,
                httpInfo.getKey(),
                httpInfo.getValue(),
                method.resolve().getQualifiedSignature(),
                inputParams,
                outputParam,
                method
        );
    }

    private static Map.Entry<String, String> resolveHttpMethodAndPath(MethodDeclaration method) {

        Optional<AnnotationExpr> mappingAnnotationOpt = method.getAnnotations().stream()
                .filter(a -> MAPPING_ANNOTATIONS.contains(a.getNameAsString()))
                .findFirst();

        if (mappingAnnotationOpt.isEmpty()) {
            return new AbstractMap.SimpleEntry<>("N/A", "");
        }

        AnnotationExpr annotation = mappingAnnotationOpt.get();
        String httpMethod = "";

        String annotationName = annotation.getNameAsString();
        if (annotationName.endsWith("Mapping")) {
            httpMethod = annotationName.substring(0, annotationName.length() - "Mapping".length()).toUpperCase();
            if (httpMethod.isEmpty()) {
                httpMethod = "N/A";
            }
        }

        String pathValue = "";

        if (annotation.isSingleMemberAnnotationExpr()) {
            Expression value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
            pathValue = extractStringValue(value).orElse("");
        }
        else if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotation = annotation.asNormalAnnotationExpr();

            Optional<Expression> pathExpression = normalAnnotation.getPairs().stream()
                    .filter(pair -> pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path"))
                    .map(pair -> pair.getValue())
                    .findFirst();

            pathValue = pathExpression.flatMap(ParseSingleMethod::extractStringValue).orElse("");
        }

        return new AbstractMap.SimpleEntry<>(httpMethod, pathValue);
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
