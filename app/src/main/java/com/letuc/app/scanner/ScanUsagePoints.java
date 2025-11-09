package com.letuc.app.scanner;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.model.SingleMethodInfo;

import java.util.*;

public class ScanUsagePoints {
    public static Map<String, SingleControllerInfo> scan(
            Map<String, SingleControllerInfo> controllerInfo,
            Map<String, MethodDeclaration> globalMethodIndex,
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
                    MethodDeclaration currentMethodAst = globalMethodIndex.get(currentFqn);
                    if (currentMethodAst == null || currentMethodAst.getBody().isEmpty()) {
                        continue;
                    }
                    List<MethodCallExpr> callsInsideThisNode = currentMethodAst.getBody().get().findAll(MethodCallExpr.class);

                    for (MethodCallExpr methodCallExpr : callsInsideThisNode) {
                        try {
                            ResolvedMethodDeclaration resolvedCall = methodCallExpr.resolve();

                            String interfaceName = resolvedCall.declaringType().getQualifiedName();
                            String nextFqn = resolvedCall.getQualifiedSignature();
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
                }
            }
        }

        return controllerInfo;
    }
}