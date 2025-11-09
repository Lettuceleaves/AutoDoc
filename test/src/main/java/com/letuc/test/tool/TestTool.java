package com.letuc.test.tool;

import lombok.Data;

@Data
public class TestTool {
    private String name;

    public static String testTool1() {
        return "TestTool";
    }

    public static String testTool3() {
        return "TestTool3";
    }

    public String testTool2() {
        testTool3();
        return name;
    }
}
