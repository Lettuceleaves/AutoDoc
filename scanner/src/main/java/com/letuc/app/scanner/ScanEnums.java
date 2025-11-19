package com.letuc.app.scanner;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.letuc.app.tool.ASTMap;

import java.util.*;

public class ScanEnums {
    
    public static final Map<String, Map<String, List<String>>> enumMap = new HashMap<>();
    
    public static void scan() {
        enumMap.clear();
        
        for (Map.Entry<String, CompilationUnit> entry : ASTMap.AST.entrySet()) {
            String className = entry.getKey();
            CompilationUnit cu = entry.getValue();
            
            List<EnumDeclaration> enums = cu.findAll(EnumDeclaration.class);
            for (EnumDeclaration enumDecl : enums) {
                String enumFqn = className;
                if (!enumDecl.isPublic()) {
                    String outerClassName = className;
                    String simpleName = enumDecl.getNameAsString();
                    enumFqn = outerClassName + "$" + simpleName;
                }
                
                Map<String, List<String>> constantsMap = new HashMap<>();
                List<StringLiteralExpr> valuesList = new ArrayList<>();
                
                for (EnumConstantDeclaration constant : enumDecl.getEntries()) {
                    String constantName = constant.getNameAsString();
                    
                    String constantValue = constantName;
                    
                    if (!constant.getArguments().isEmpty()) {
                        Expression arg = constant.getArguments().get(0);
                        if (arg.isStringLiteralExpr()) {
                            constantValue = arg.asStringLiteralExpr().asString();
                            valuesList.add(arg.asStringLiteralExpr());
                        }
                    }
                    List<String> values = new ArrayList<>();
                    values.add(constantValue);
                    
                    if (constant.getArguments().size() >= 2) {
                        for (int i = 1; i < constant.getArguments().size(); i++) {
                            Expression arg = constant.getArguments().get(i);
                            if (arg.isStringLiteralExpr()) {
                                values.add(arg.asStringLiteralExpr().asString());
                            } else {
                                values.add(arg.toString());
                            }
                        }
                    }
                    
                    constantsMap.put(constantName, values);
                }
                enumMap.put(enumFqn, constantsMap);
                System.out.println("扫描到枚举类: " + enumFqn + "，包含 " + constantsMap.size() + " 个常量");
            }
        }
        print();
        System.out.println("枚举类扫描完成，共扫描到 " + enumMap.size() + " 个枚举类");
    }

    public static Map<String, List<String>> getEnumConstantsMap(String enumClassName) {
        if (enumClassName == null || enumClassName.isEmpty()) {
            return Collections.emptyMap();
        }
        return enumMap.getOrDefault(enumClassName, Collections.emptyMap());
    }

    public static boolean isEnumClass(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        if (enumMap.containsKey(className)) {
            return true;
        }
        for (String key : enumMap.keySet()) {
            if (key.endsWith("." + className) || key.endsWith("$" + className)) {
                return true;
            }
        }
        return false;
    }

    public static void print() {
        for (Map.Entry<String, Map<String, List<String>>> entry : enumMap.entrySet()) {
            String enumClassName = entry.getKey();
            Map<String, List<String>> constantsMap = entry.getValue();
            System.out.println("枚举类: " + enumClassName);
            for (Map.Entry<String, List<String>> constantEntry : constantsMap.entrySet()) {
                System.out.println("  常量: " + constantEntry.getKey() + " = " + constantEntry.getValue());
            }
        }
    }
}