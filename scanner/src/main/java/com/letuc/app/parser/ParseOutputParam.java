package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.letuc.app.model.OutputParam;
import com.letuc.app.model.OutputParamCode;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.ConfigMap;
import com.letuc.app.tool.GenericStack;
import com.letuc.app.tool.ParseRecursiveEndPoint;
import com.letuc.app.tool.SymbolSolver;

import java.util.*;

public class ParseOutputParam {

    private static final ThreadLocal<Set<String>> RECURSION_GUARD = ThreadLocal.withInitial(HashSet::new);

    public static void parse(Map<String, SingleMethodInfo> controllers) {
        try {
            for (Map.Entry<String, SingleMethodInfo> singleMethodInfo : controllers.entrySet()) {
                RECURSION_GUARD.get().clear();

                OutputParam output = singleMethodInfo.getValue().getOutputParam();

                parseOutputParam(output);

                if (output != null && output.getClassName() != null) {
                    ConfigMap.targets.add(output.getClassName());
                }
            }
        } finally {
            RECURSION_GUARD.remove();
        }
    }

    public static void parseOutputParam(OutputParam outputParam) {
        String currentTypeKey = outputParam.getClassName();
        if (RECURSION_GUARD.get().contains(currentTypeKey)) {
            return;
        }
        RECURSION_GUARD.get().add(currentTypeKey);

        List<OutputParam> subParams = new ArrayList<>();
        try {
            ResolvedReferenceTypeDeclaration typeDecl =
                    SymbolSolver.combinedTypeSolver.solveType(outputParam.getClassName());

            String typeSignature = outputParam.getOrigin() != null ? outputParam.getOrigin() : outputParam.getClassName();
            GenericStack genericStack = new GenericStack(typeSignature);

            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                try {
                    if (field.isStatic()) {
                        continue;
                    }

                    ResolvedType fieldType = field.getType();
                    String rawClassName = fieldType.describe();
                    String fieldName = field.getName();
                    String actualClassName = rawClassName;
                    String origin = null;

                    if (fieldType.isTypeVariable()) {
                        actualClassName = genericStack.next();
                        origin = actualClassName;
                    }
                    else if (isCollectionOrArray(fieldType)) {
                        if (rawClassName.contains("<")) {
                            String innerType = rawClassName.substring(rawClassName.indexOf('<') + 1, rawClassName.lastIndexOf('>'));
                            if (!fieldType.asReferenceType().getTypeParametersMap().isEmpty()) {
                                if (innerType.length() == 1) {
                                    actualClassName = genericStack.next();
                                } else {
                                    actualClassName = innerType;
                                }
                            }
                            origin = actualClassName;
                        }
                    }
                    OutputParam fieldParam;
                    boolean isLeafNode = ParseRecursiveEndPoint.set.contains(actualClassName) || isBasicType(actualClassName);

                    if (isLeafNode) {
                        if ("code".equals(fieldName)) {
                            List<String> enumValues = new ArrayList<>();

                            fieldParam = new OutputParamCode(
                                    actualClassName,
                                    origin,
                                    fieldName,
                                    null,
                                    null,
                                    enumValues
                            );
                        } else {
                            fieldParam = new OutputParam(
                                    actualClassName,
                                    origin,
                                    fieldName,
                                    null
                            );
                        }
                        subParams.add(fieldParam);
                    } else {
                        String nextOrigin = (origin != null) ? origin : actualClassName;
                        String cleanClassName = actualClassName.contains("<") ?
                                actualClassName.substring(0, actualClassName.indexOf('<')) : actualClassName;

                        fieldParam = new OutputParam(cleanClassName, nextOrigin, fieldName, null);

                        if (canResolveType(cleanClassName)) {
                            parseOutputParam(fieldParam);
                        } else {
                            fieldParam.setSubParams(null);
                        }
                        subParams.add(fieldParam);
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            return;
        } finally {
            RECURSION_GUARD.get().remove(outputParam.getClassName());
        }

        outputParam.setSubParams(subParams);
    }

    private static boolean canResolveType(String typeName) {
        if (typeName.startsWith("java.") || typeName.startsWith("javax.")) {
            return false;
        }
        try {
            SymbolSolver.combinedTypeSolver.solveType(typeName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCollectionOrArray(ResolvedType type) {
        if (type.isArray()) return true;
        if (type.isReferenceType()) {
            String name = type.asReferenceType().getQualifiedName();
            return name.equals("java.util.List") ||
                    name.equals("java.util.Set") ||
                    name.equals("java.util.Map") ||
                    name.equals("java.util.Collection");
        }
        return false;
    }

    private static boolean isBasicType(String typeName) {
        return typeName.equals("int") || typeName.equals("long") || typeName.equals("boolean") ||
                typeName.equals("double") || typeName.equals("float") || typeName.equals("char") ||
                typeName.equals("byte") || typeName.equals("short");
    }
}