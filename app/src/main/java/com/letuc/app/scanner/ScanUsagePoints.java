package com.letuc.app.scanner;

import com.letuc.app.model.InitMethodInfo;
import com.letuc.app.model.SingleControllerInfo;
import com.letuc.app.model.SingleMethodInfo;

import java.util.List;
import java.util.Map;

public class ScanUsagePoints {
    public static Map<String, SingleControllerInfo> scan(Map<String, SingleControllerInfo> controllerInfo) {
        for (SingleControllerInfo singleControllerInfo : controllerInfo.values()) {
            Map<String, SingleMethodInfo> methods = singleControllerInfo.getControllerMap();
            for (SingleMethodInfo singleMethodInfo : methods.values()) {
                List<InitMethodInfo> initMethods = singleMethodInfo.getOutputParam().getMethods();
                String signature = singleMethodInfo.getSignature();
            }
        }
        return null;
    }
}
