package com.letuc.app.scanner;

import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.parser.ParseSingleController;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanControllers {
    public static Map<String, SingleControllerInfo>  scan(List<Path> javaFiles) {
        Map<String, SingleControllerInfo> controllerInfo = new HashMap<>();
        for (Path javaFile : javaFiles) {
            SingleControllerInfo singleControllerInfo = ParseSingleController.parse(javaFile);
            if (singleControllerInfo != null) {
                controllerInfo.put(javaFile.toString(), singleControllerInfo);
            }
        }
        return controllerInfo;
    }
}
