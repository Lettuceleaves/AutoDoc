package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.letuc.app.model.InputParam;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.GenericStack;
import com.letuc.app.tool.ParseRecursiveEndPoint;
import com.letuc.app.tool.SymbolSolver;

import java.util.*;

public class ParseInputParams {

    // 【核心修复 1】 防止无限递归：记录正在解析的类型链
    private static final ThreadLocal<Set<String>> RECURSION_GUARD = ThreadLocal.withInitial(HashSet::new);

    public static void parse(Map<String, SingleMethodInfo> methodInfoMap) {
        try {
            for (SingleMethodInfo methodInfo : methodInfoMap.values()) {
                if (methodInfo.getInputParams() != null) {
                    for (InputParam inputParam : methodInfo.getInputParams()) {
                        // 每次解析一个新的根参数前，清空守卫（或者让 parseInputParam 自己管理）
                        // 这里建议依靠 parseInputParam 内部的 add/remove 机制
                        RECURSION_GUARD.get().clear();
                        parseInputParam(inputParam);
                    }
                }
            }
        } finally {
            RECURSION_GUARD.remove();
        }
    }

    public static void parseInputParam(InputParam inputParam) {
        // 【核心修复 2】 检查是否形成环
        String currentTypeKey = inputParam.getType();
        // 如果当前类型已经在栈中，说明发生了循环引用（A -> B -> A），直接返回，不再递归
        if (RECURSION_GUARD.get().contains(currentTypeKey)) {
            return;
        }
        RECURSION_GUARD.get().add(currentTypeKey);

        List<InputParam> subParams = new ArrayList<>();

        try {
            // 1. 如果是基本类型或终点类型，不解析
            if (ParseRecursiveEndPoint.set.contains(inputParam.getType()) || isBasicType(inputParam.getType())) {
                return;
            }

            // 2. 尝试解析类型定义
            ResolvedReferenceTypeDeclaration typeDecl =
                    SymbolSolver.combinedTypeSolver.solveType(inputParam.getType());

            // 处理泛型
            // 如果 inputParam 里的 type 已经是具体类型（如 UserDTO），genericStack 初始化为它
            GenericStack genericStack = new GenericStack(inputParam.getType());

            // 3. 遍历字段
            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                try {
                    if (field.isStatic()) {
                        continue;
                    }

                    ResolvedType fieldType = field.getType();
                    String rawClassName = fieldType.describe();
                    String fieldName = field.getName();
                    String actualClassName = rawClassName;

                    // 处理泛型 T data
                    if (fieldType.isTypeVariable()) {
                        actualClassName = genericStack.next();
                    }
                    // 处理集合 List<T>
                    else if (isCollectionOrArray(fieldType)) {
                        if (rawClassName.contains("<")) {
                            String innerType = rawClassName.substring(rawClassName.indexOf('<') + 1, rawClassName.lastIndexOf('>'));
                            if (fieldType.asReferenceType().getTypeParametersMap().size() > 0) {
                                if (innerType.length() == 1) {
                                    actualClassName = genericStack.next();
                                } else {
                                    actualClassName = innerType;
                                }
                            }
                        }
                    }

                    // 创建子参数对象
                    InputParam subParam = new InputParam();
                    subParam.setName(fieldName);

                    // 清洗类型名称 (去掉 <...>)
                    String cleanType = actualClassName.contains("<") ?
                            actualClassName.substring(0, actualClassName.indexOf('<')) : actualClassName;

                    subParam.setType(cleanType);
                    // 这里 field 通常不直接对应 BODY/QUERY，可以置空或继承逻辑
                    subParam.setField(null);

                    // 【核心修复 3】 只有能解析且不是环的时候才递归
                    if (canResolveType(cleanType)) {
                        // 递归调用！这里如果没有 RECURSION_GUARD 就会死循环
                        parseInputParam(subParam);
                    }

                    subParams.add(subParam);

                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            // 解析失败忽略
        } finally {
            // 【核心修复 4】 退出当前层级时，移除标记
            RECURSION_GUARD.get().remove(inputParam.getType());
        }

        if (!subParams.isEmpty()) {
            inputParam.setSubParams(subParams);
        }
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
                typeName.equals("byte") || typeName.equals("short") || typeName.equals("java.lang.String");
    }
}