package com.letuc.app.scanner;

import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.parser.ParseSingleController;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanControllers {
    public static Map<String, SingleControllerInfo>  scan(List<Path> javaFiles, String controllerTail) {
        Map<String, SingleControllerInfo> controllerInfo = new HashMap<>();
        for (Path javaFile : javaFiles) {
            if (isController(javaFile, controllerTail)) {
                SingleControllerInfo singleControllerInfo = ParseSingleController.parse(javaFile);
                controllerInfo.put(javaFile.toString(), singleControllerInfo);
            }
        }
        return controllerInfo;
    }

    private static boolean isController(Path javaFile, String controllerTail) {
        return  javaFile.toString().endsWith(controllerTail);
    }
}
