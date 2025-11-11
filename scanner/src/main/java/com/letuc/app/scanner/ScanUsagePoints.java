package com.letuc.app.scanner;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.letuc.app.export.JSON;
import com.letuc.app.export.MarkDown;
import com.letuc.app.model.OutputParamString;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.model.SingleMethodInfo;
import com.letuc.app.tool.ASTMap;

import java.util.*;

import static com.github.javaparser.ast.Node.SYMBOL_RESOLVER_KEY;

public class ScanUsagePoints {
    public static Map<String, SingleControllerInfo> scan(
            Map<String, SingleControllerInfo> controllerInfo,
            Map<String, String> pairs) throws Exception {

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
                        System.err.println("找不到 " + classFqn + " 对应的AST (来自 " + currentFqn + ")");
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


                            if (singleMethodInfo.getOutputParam().getMethodsFilter().contains(nextFqn)) {
                                System.out.print("""
                                
                                命中目标方法
                                
                                """);
                                NodeList<Expression> arguments = methodCallExpr.getArguments();
                                for (int i = 0; i < arguments.size(); i++) {
                                    if (arguments.get(i).isStringLiteralExpr() && singleMethodInfo.getOutputParam().getSubParams().get(i) instanceof OutputParamString outputParamString) {
                                        outputParamString.getValues().add(arguments.get(i).toString());
                                        System.out.println("属性" + outputParamString.getClassName() + "可能的值：" + arguments.get(i).toString());
                                    }
                                }
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
                            System.err.println("  -> 解析失败: " + methodCallExpr.getNameAsString() + " (原因: " + e.getClass().getSimpleName() + ")");
                        }
                    }

                    List<ObjectCreationExpr> creationsInsideThisNode = currentMethodAst.getBody().get().findAll(ObjectCreationExpr.class);

                    for (ObjectCreationExpr creationExpr : creationsInsideThisNode) {
                        try {
                            ResolvedConstructorDeclaration resolvedConstructor = creationExpr.resolve();

                            String nextFqn = resolvedConstructor.getQualifiedSignature();

                            if (singleMethodInfo.getOutputParam().getMethodsFilter().contains(nextFqn)) {
                                System.out.print("""
                                
                                命中目标方法，参数：
                                """);
                                NodeList<Expression> arguments = creationExpr.getArguments();
                                for (int i = 0; i < arguments.size(); i++) {
                                    if (arguments.get(i).isStringLiteralExpr() && singleMethodInfo.getOutputParam().getSubParams().get(i) instanceof OutputParamString outputParamString) {
                                        outputParamString.getValues().add(arguments.get(i).toString());
                                        System.out.println("属性" + outputParamString.getClassName() + "可能的值：" + arguments.get(i).toString());
                                    }
                                }
                            }

                            if (nextFqn != null && !visitedSignatures.contains(nextFqn)) {
                                visitedSignatures.add(nextFqn);
                                queue.add(nextFqn);
                                System.out.println("  -> [实例] 发现新调用: " + nextFqn);
                            }
                        } catch (Exception e) {
                            System.err.println("  -> [实例] 解析失败: " + creationExpr.getType().getNameAsString() + " (原因: " + e + ")");
                        }
                    }
                }
            }
        }
        System.out.println("接口序列化结果：");
        StringBuilder sb  = new StringBuilder();
        for (SingleControllerInfo singleControllerInfo : controllerInfo.values()) {
            System.out.println(singleControllerInfo.toJson());
            sb.append(singleControllerInfo.toJson());
        }
        MarkDown.saveToFile(sb.toString(), "temp/my_generated_report.md");
        JSON.saveToFile(sb.toString(), "temp/my_generated_report.json");
        return controllerInfo;
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