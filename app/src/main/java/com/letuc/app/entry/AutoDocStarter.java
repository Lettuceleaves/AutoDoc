package com.letuc.app.entry;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.scanner.*;
import com.letuc.app.tool.InterfaceToBean;
import com.letuc.app.tool.SymbolSolver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoDocStarter {
    public static String controllerTail = "Controller.java";

    public static void run() {
        try {
            List<Path> sourceRoots = ScanConfig.collectJavaSourcePaths(Paths.get(System.getProperty("user.dir")));
            SymbolSolver.init(sourceRoots);
            List<Path> allJavaFiles = ScanFilePaths.scan(sourceRoots);
            System.out.println("开始构建全局方法索引...");
            Map<String, MethodDeclaration> globalMethodIndex = buildMap(allJavaFiles);
            System.out.println("索引构建完毕，共 " + globalMethodIndex.size() + " 个方法。");

            Map<String, SingleControllerInfo> controllerInfo = ScanControllers.scan(allJavaFiles, controllerTail);
            ScanDI.scan(allJavaFiles);
            InterfaceToBean.print();

            System.out.println("开始扫描调用链 (BFS)...");
            controllerInfo = ScanUsagePoints.scan(
                    controllerInfo,
                    globalMethodIndex,
                    InterfaceToBean.pairs
            );
            System.out.println("调用链扫描完毕。");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static Map<String, MethodDeclaration> buildMap(List<Path> javaFiles) {
        Map<String, MethodDeclaration> globalMethodIndex = new HashMap<>();

        for (Path path : javaFiles) {
            File javaFile = path.toFile();
            if (!javaFile.exists()) continue;

            try {
                CompilationUnit cu = StaticJavaParser.parse(javaFile);
                List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);

                for (MethodDeclaration method : methods) {
                    try {
                        String fqn = method.resolve().getQualifiedSignature();
                        globalMethodIndex.put(fqn, method);
                    } catch (Exception ignore) {
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return globalMethodIndex;
    }
}