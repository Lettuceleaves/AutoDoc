package com.letuc.app.parser;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.letuc.app.model.OutputParam;
import com.letuc.app.model.OutputParamString;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.GenericStack;
import com.letuc.app.tool.ParseRecursiveEndPoint;
import com.letuc.app.tool.SymbolSolver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParseOutputParam {
    public static void parse(Map<String, SingleMethodInfo> controllers) {
        // 第一遍：先填充第一层参数结构
        for (Map.Entry<String, SingleMethodInfo> singleMethodInfo : controllers.entrySet()) {
            // 只解析第一层参数
            parseFirstLevelOutputParam(singleMethodInfo.getValue().getOutputParam());
        }
        
        // 第二遍：调用addAllArgsConstructorToInitMethods()并递归解析更深层次
        for (Map.Entry<String, SingleMethodInfo> singleMethodInfo : controllers.entrySet()) {
            OutputParam outputParam = singleMethodInfo.getValue().getOutputParam();
            outputParam.addAllArgsConstructorToInitMethods();
            // 递归解析更深层次的参数
            recursivelyParseOutputParam(outputParam);
        }
    }
    // 只解析第一层参数，不进行递归，第一层不替换泛型参数
    public static void parseFirstLevelOutputParam(OutputParam outputParam) {
        List<OutputParam> subParams = new ArrayList<>();
        try {
            ResolvedReferenceTypeDeclaration typeDecl = 
                    SymbolSolver.combinedTypeSolver.solveType(outputParam.getClassName());
            // 第一层不需要创建GenericStack，保持泛型参数不变
            for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                try {
                    if (field.isStatic()) {
                        continue;
                    }
                    String className = field.getType().describe();
                    String name = field.getName();
                    // 第一层不替换泛型参数
                    // 移除if (field.getType().isTypeVariable()) {...} 逻辑
                    
                    // 创建参数对象，但不进行递归解析
                    if (name.equals("code")) { // TODO 整合到配置文件中
                        OutputParamString fieldParam = new OutputParamString(className, null, name, null, null, null, new LinkedList<>(), new LinkedList<>());
                        subParams.add(fieldParam);
                    } else {
                        OutputParam fieldParam = new OutputParam(className, null, name, null, null, null);
                        // 先设置subParams为null，表示尚未递归解析
                        fieldParam.setSubParams(null);
                        subParams.add(fieldParam);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            // 如果无法解析类型，设置空列表
            subParams = new ArrayList<>();
        }
        
        outputParam.setSubParams(subParams);
    }
    
    // 递归解析更深层次的参数
    public static void recursivelyParseOutputParam(OutputParam outputParam) {
        if (outputParam.getSubParams() == null || outputParam.getSubParams().isEmpty()) {
            return;
        }
        
        for (OutputParam subParam : outputParam.getSubParams()) {
            // 跳过终止点类型和已经解析过的参数
            if (ParseRecursiveEndPoint.set.contains(subParam.getClassName()) || 
                subParam.getSubParams() != null) {
                continue;
            }
            
            // 递归解析子参数
            List<OutputParam> deeperSubParams = new ArrayList<>();
            try {
                ResolvedReferenceTypeDeclaration typeDecl = 
                        SymbolSolver.combinedTypeSolver.solveType(subParam.getClassName());
                GenericStack genericStack = new GenericStack(subParam.getOrigin());
                for (ResolvedFieldDeclaration field : typeDecl.getDeclaredFields()) {
                    try {
                        if (field.isStatic()) {
                            continue;
                        }
                        String className = field.getType().describe();
                        String name = field.getName();
                        if (field.getType().isTypeVariable()) {
                            className = genericStack.next();
                            System.out.println(field.getType().toString());
                        }
                        
                        if (ParseRecursiveEndPoint.set.contains(className)) {
                            OutputParamString fieldParam = new OutputParamString(className, null, name, null, null, null, new LinkedList<>(), new LinkedList<>());
                            deeperSubParams.add(fieldParam);
                        } else {
                            OutputParam fieldParam = new OutputParam(className, null, name, null, null, null);
                            fieldParam.setSubParams(null); // 标记为未解析
                            deeperSubParams.add(fieldParam);
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                // 如果无法解析类型，设置空列表
                deeperSubParams = new ArrayList<>();
            }
            
            subParam.setSubParams(deeperSubParams);
            
            // 继续递归解析更深层次
            recursivelyParseOutputParam(subParam);
        }
    }
    
    // 保留原方法作为兼容
    public static void parseOutputParam(OutputParam outputParam) {
        parseFirstLevelOutputParam(outputParam);
        recursivelyParseOutputParam(outputParam);
    }

    private static boolean canResolveType(String typeName) {
        try {
            SymbolSolver.combinedTypeSolver.solveType(typeName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
