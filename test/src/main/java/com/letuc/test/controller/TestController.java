package com.letuc.test.controller;

import com.letuc.test.model.UserDO;
import com.letuc.test.model.UserDTO;
import com.letuc.test.result.ResultVO;
import com.letuc.test.service.TestService;
import com.letuc.test.tool.TestTool;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

    private TestService testService;

    @GetMapping("/test1")
    ResultVO<UserDTO> test1(UserDTO data) {
        TestTool testTool = new TestTool();
        TestTool.testTool1();
        testTool.testTool2();
        return new ResultVO<>("0", new UserDTO(), "success");
    }

    @PostMapping("/test2")
    ResultVO test2(UserDTO data) {
        return new ResultVO<>("0", new UserDTO(), "success");
    }

    @GetMapping("/test3")
    ResultVO<UserDO> test3(UserDTO data) {
        return testService.test(data);
    }
}