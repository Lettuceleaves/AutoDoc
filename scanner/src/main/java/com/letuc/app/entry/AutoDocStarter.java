package com.letuc.app.entry;

import com.letuc.app.export.JSON;
import com.letuc.app.export.MarkDown;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.scanner.ScanControllers;
import com.letuc.app.scanner.ScanDI;
import com.letuc.app.scanner.ScanEnums;
import com.letuc.app.scanner.ScanFilePaths;
import com.letuc.app.scanner.ScanUsagePoints;
import com.letuc.app.tool.ASTMap;
import com.letuc.app.tool.ConfigMap;
import com.letuc.app.tool.InterfaceToBean;
import com.letuc.app.tool.SymbolSolver;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.letuc.app.tool.ConfigMap.collectTargetMethods;

public class AutoDocStarter {

    public static void run() {
        run("test");
    }

    public static void run(String configPath) {
        try {
            ConfigMap.init(configPath);
            SymbolSolver.init();
            List<Path> allJavaFiles = ScanFilePaths.scan();
            System.out.println("开始构建全局方法索引...");
            ASTMap.buildMap(allJavaFiles);
            collectTargetMethods();
            System.out.println("索引构建完毕，共 " + ASTMap.AST.size() + " 个类。");

            Map<String, SingleControllerInfo> controllerInfo = ScanControllers.scan(allJavaFiles);
            ScanDI.scan(allJavaFiles);
            ScanEnums.scan();
            InterfaceToBean.print();

            System.out.println("开始扫描调用链 (BFS)...");
            ScanUsagePoints.scan(
                    controllerInfo,
                    InterfaceToBean.pairs
            );
            System.out.println("接口序列化结果：");
            StringBuilder sb  = new StringBuilder();
            for (SingleControllerInfo singleControllerInfo : controllerInfo.values()) {
                System.out.println(singleControllerInfo.toJson());
                sb.append(singleControllerInfo.toJson());
            }
            MarkDown.saveToFile(sb.toString(), ConfigMap.getExpMarkdownFilePath());
            JSON.saveToFile(sb.toString(), ConfigMap.getExpJSONFilePath());
            System.out.println("调用链扫描完毕。");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}