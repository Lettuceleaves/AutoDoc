package com.letuc.app.tool;

import java.util.HashMap;
import java.util.Map;

public class InterfaceToBean {
    public static Map<String, String> pairs = new HashMap<>();

    public static String get(String key) {
        return pairs.getOrDefault(key, null);
    }

    public static void set(String key, String value) {
        pairs.put(key, value);
    }

    public static void print() {
        System.out.println("\n---------- 接口与 bean 的全部映射如下 ----------\n");
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            System.out.println(pair.getKey() + ": " + pair.getValue());
        }
    }
}
