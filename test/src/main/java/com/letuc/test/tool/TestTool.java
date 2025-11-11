package com.letuc.test.tool;

import com.letuc.test.model.UserDTO;
import lombok.Data;

@Data
public class TestTool {
    private String name;

    public static String testTool1(UserDTO userDTO) {
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
