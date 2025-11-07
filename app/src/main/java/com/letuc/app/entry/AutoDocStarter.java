package com.letuc.app.entry;

import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.scanner.ScanControllers;
import com.letuc.app.scanner.ScanDI;
import com.letuc.app.scanner.ScanUsagePoints;
import com.letuc.app.tool.FileCollector;
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
            List<Path> javaFiles = FileCollector.collectJavaFiles(Paths.get(System.getProperty("user.dir")));
            SymbolSolver.init(List.of(Path.of("test/src/main/java")));
            Map<String, SingleControllerInfo> controllerInfo = ScanControllers.scan(javaFiles, controllerTail);
            ScanDI.scan(javaFiles);
            InterfaceToBean.print();
            controllerInfo = ScanUsagePoints.scan(controllerInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
