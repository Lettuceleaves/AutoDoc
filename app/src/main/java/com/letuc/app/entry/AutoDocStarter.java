package com.letuc.app.entry;

import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.scanner.*;
import com.letuc.app.tool.ASTMap;
import com.letuc.app.tool.InterfaceToBean;
import com.letuc.app.tool.SymbolSolver;

import java.nio.file.Path;
import java.nio.file.Paths;
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
            ASTMap.buildMap(allJavaFiles);
            System.out.println("索引构建完毕，共 " + ASTMap.AST.size() + " 个类。");

            Map<String, SingleControllerInfo> controllerInfo = ScanControllers.scan(allJavaFiles, controllerTail);
            ScanDI.scan(allJavaFiles);
            InterfaceToBean.print();

            System.out.println("开始扫描调用链 (BFS)...");
            controllerInfo = ScanUsagePoints.scan(
                    controllerInfo,
                    InterfaceToBean.pairs
            );
            System.out.println("调用链扫描完毕。");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}