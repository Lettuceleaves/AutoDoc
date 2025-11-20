package com.letuc.app.scanner;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.letuc.app.model.OutputParamCode;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.ASTMap;
import com.letuc.app.tool.ConfigMap;

import java.util.*;

import static com.github.javaparser.ast.Node.SYMBOL_RESOLVER_KEY;

public class ScanUsagePoints {

    private static final int TYPE_STRING_LITERAL = 0;
    private static final int TYPE_ENUM = 1;

    private static final String SUCCESS_METHOD = "com.letuc.test.result.Result.success";
    private static final String FAILURE_METHOD = "com.letuc.test.result.Result.failure";

    public static void scan(
            Map<String, SingleControllerInfo> controllerInfo,
            Map<String, String> pairs) {

        for (SingleControllerInfo singleControllerInfo : controllerInfo.values()) {
            Map<String, SingleMethodInfo> methods = singleControllerInfo.getControllerMap();
            for (SingleMethodInfo singleMethodInfo : methods.values()) {
                MethodDeclaration ast = singleMethodInfo.getMethodNode();
                if (ast == null || ast.getBody().isEmpty()) {
                    continue;
                }
                Queue<String> queue = new LinkedList<>();
                Set<String> visitedSignatures = new HashSet<>();

                try {
                    String startFqn = ast.resolve().getQualifiedSignature();
                    queue.add(startFqn);
                    visitedSignatures.add(startFqn);
                    System.out.println("--- 开始扫描方法: " + startFqn + " ---");
                } catch (Exception e) {
                    System.err.println("!!! 无法解析起始方法: " + singleMethodInfo.getSignature());
                    continue;
                }

                while (!queue.isEmpty()) {
                    String currentFqn = queue.poll();
                    String classFqn = extractClassFqn(currentFqn);
                    if (classFqn == null) {
                        System.err.println("!!! 无法从 " + currentFqn + " 提取类名");
                        continue;
                    }
                    CompilationUnit cu = ASTMap.AST.get(classFqn);
                    if (cu == null) {
                        System.out.println("找不到 " + classFqn + " 对应的AST (来自 " + currentFqn + ")");
                        continue;
                    }
                    cu.setData(
                            SYMBOL_RESOLVER_KEY,
                            new com.github.javaparser.symbolsolver.JavaSymbolSolver(
                                    com.letuc.app.tool.SymbolSolver.combinedTypeSolver
                            )
                    );
                    MethodDeclaration currentMethodAst = findMethodByFqn(cu, currentFqn);
                    if (currentMethodAst == null || currentMethodAst.getBody().isEmpty()) {
                        continue;
                    }
                    List<MethodCallExpr> callsInsideThisNode = currentMethodAst.getBody().get().findAll(MethodCallExpr.class);

                    for (MethodCallExpr methodCallExpr : callsInsideThisNode) {
                        try {
                            ResolvedMethodDeclaration resolvedCall = methodCallExpr.resolve();

                            String interfaceName = resolvedCall.declaringType().getQualifiedName();
                            String nextFqn = resolvedCall.getQualifiedSignature();
                            String typeFQN = resolvedCall.declaringType().getQualifiedName();
                            String methodName = resolvedCall.getName();
                            String currentMethodFQN = typeFQN + "." + methodName;


                            if (ConfigMap.targets.contains(currentMethodFQN)) {
                                System.out.print("""
                                
                                命中目标方法
                                
                                """);

                                NodeList<Expression> arguments = methodCallExpr.getArguments();

                                if (!arguments.isEmpty()) {
                                    Expression firstArg = arguments.get(0);

                                    // 1. 处理 String 字面量
                                    if (firstArg.isStringLiteralExpr()) {
                                        String value = firstArg.asStringLiteralExpr().asString();
                                        System.out.println("提取到字符串参数: " + value);
                                        if (singleMethodInfo.getOutputParam().getSubParams().get(0) instanceof OutputParamCode outputParamCode) {
                                            // 【修改】直接调用 addEntry，自动处理 Set 去重
                                            outputParamCode.addEntry(TYPE_STRING_LITERAL, value);
                                        }
                                    }

                                    // 2. 处理枚举/字段引用
                                    else if (firstArg.isFieldAccessExpr() || firstArg.isNameExpr()) {
                                        try {
                                            ResolvedValueDeclaration resolvedDecl;

                                            if (firstArg.isFieldAccessExpr()) {
                                                resolvedDecl = firstArg.asFieldAccessExpr().resolve();
                                            } else {
                                                resolvedDecl = firstArg.asNameExpr().resolve();
                                            }

                                            if (resolvedDecl instanceof ResolvedEnumConstantDeclaration enumConst) {

                                                String constName = enumConst.getName();
                                                String enumClassFqn = enumConst.getType().asReferenceType().getQualifiedName();

                                                Map<String, List<String>> enumData = ScanEnums.getEnumConstantsMap(enumClassFqn);

                                                if (!enumData.isEmpty() && enumData.containsKey(constName)) {
                                                    List<String> values = enumData.get(constName);
                                                    System.out.println("提取到枚举参数 [解析成功]: " + enumClassFqn + "." + constName);
                                                    System.out.println("  -> 枚举详细值: " + values);
                                                    String enumString = enumClassFqn + "." + constName;

                                                    if (singleMethodInfo.getOutputParam().getSubParams().get(0) instanceof OutputParamCode outputParamCode) {
                                                        // 【修改】直接调用 addEntry，Type 为 1
                                                        outputParamCode.addEntry(TYPE_ENUM, enumString);
                                                    }
                                                } else {
                                                    System.err.println("警告: 代码中使用了枚举 " + enumClassFqn + "." + constName + "，但在 ScanEnums 结果中未找到对应定义。可能未包含在扫描路径中。");
                                                }
                                            } else {
                                                System.out.println("忽略非枚举常量引用: " + firstArg);
                                            }

                                        } catch (Exception e) {
                                            System.err.println("SymbolSolver 解析参数失败 [" + firstArg + "]: " + e.getMessage());
                                        }
                                    }
                                } else {
                                    // 3. 处理无参默认方法 (Success/Failure)
                                    if (currentMethodFQN.equals(SUCCESS_METHOD)) {
                                        if (singleMethodInfo.getOutputParam().getSubParams().get(0) instanceof OutputParamCode outputParamCode) {
                                            // 【修改】直接调用 addEntry
                                            outputParamCode.addEntry(TYPE_STRING_LITERAL, "0");
                                        }
                                    } else if (currentMethodFQN.equals(FAILURE_METHOD)) {
                                        if (singleMethodInfo.getOutputParam().getSubParams().get(0) instanceof OutputParamCode outputParamCode) {
                                            // 【修改】直接调用 addEntry
                                            outputParamCode.addEntry(TYPE_STRING_LITERAL, "B010001");
                                        }
                                    } else {
                                        System.err.println("未提供code");
                                    }
                                }
                                continue;
                            }

                            if (pairs.containsKey(interfaceName)) {
                                String implName = pairs.get(interfaceName);
                                String interfaceFqn = resolvedCall.getQualifiedSignature();
                                String methodSignature = interfaceFqn.substring(interfaceName.length() + 1);
                                nextFqn = implName + "." + methodSignature;
                            }
                            if (nextFqn != null && !visitedSignatures.contains(nextFqn)) {
                                visitedSignatures.add(nextFqn);
                                queue.add(nextFqn);
                                System.out.println("  -> 发现新调用: " + nextFqn);
                            }
                        } catch (Exception e) {
                            System.out.println("  -> 解析失败: " + methodCallExpr.getNameAsString() + " (原因: " + e.getClass().getSimpleName() + ")");
                        }
                    }

                    List<ObjectCreationExpr> creationsInsideThisNode = currentMethodAst.getBody().get().findAll(ObjectCreationExpr.class);

                    for (ObjectCreationExpr creationExpr : creationsInsideThisNode) {
                        try {
                            ResolvedConstructorDeclaration resolvedConstructor = creationExpr.resolve();

                            String nextFqn = resolvedConstructor.getQualifiedSignature();
                            String currentMethodFQN = resolvedConstructor.declaringType().getQualifiedName();

                            if (ConfigMap.targets.contains(currentMethodFQN)) {
                                System.out.print("""
                                
                                命中目标方法，参数：
                                """);
                                NodeList<Expression> arguments = creationExpr.getArguments();
                                for (int i = 0; i < arguments.size(); i++) {
                                    // 4. 处理构造函数中的字符串参数
                                    if (arguments.get(i).isStringLiteralExpr() && singleMethodInfo.getOutputParam().getSubParams().get(i) instanceof OutputParamCode outputParamCode) {
                                        String val = arguments.get(i).asStringLiteralExpr().asString();

                                        // 【修改】直接调用 addEntry
                                        outputParamCode.addEntry(TYPE_STRING_LITERAL, val);

                                        System.out.println("属性" + outputParamCode.getClassName() + "可能的值：" + val);
                                    }
                                }
                            }

                            if (nextFqn != null && !visitedSignatures.contains(nextFqn)) {
                                visitedSignatures.add(nextFqn);
                                queue.add(nextFqn);
                                System.out.println("  -> [实例] 发现新调用: " + nextFqn);
                            }
                        } catch (Exception e) {
                            System.out.println("  -> [实例] 解析失败: " + creationExpr.getType().getNameAsString() + " (原因: " + e + ")");
                        }
                    }
                }
            }
        }
    }

    private static MethodDeclaration findMethodByFqn(CompilationUnit cu, String methodFqn) {
        List<MethodDeclaration> allMethodsInCu = cu.findAll(MethodDeclaration.class);
        for (MethodDeclaration method : allMethodsInCu) {
            try {
                String currentMethodFqn = method.resolve().getQualifiedSignature();
                if (currentMethodFqn.equals(methodFqn)) {
                    return method;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
    private static String extractClassFqn(String methodFqn) {
        int paren = methodFqn.indexOf('(');
        if (paren == -1) return null;

        String classAndMethod = methodFqn.substring(0, paren);
        int lastDot = classAndMethod.lastIndexOf('.');

        if (lastDot == -1) return null;

        return methodFqn.substring(0, lastDot);
    }
}