package com.letuc.app.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.letuc.app.export.JSON;
import com.letuc.app.export.MarkDown;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.scanner.*;
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
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String jsonResult = mapper.writeValueAsString(controllerInfo.values());
            System.out.println(jsonResult);
            MarkDown.saveToFile(jsonResult, ConfigMap.getExpMarkdownFilePath());
            JSON.saveToFile(jsonResult, ConfigMap.getExpJSONFilePath());
            System.out.println("调用链扫描完毕。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}