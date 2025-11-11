package com.letuc.test.controller;

import com.letuc.test.model.UserDTO;
import com.letuc.test.result.ResultVO;
import com.letuc.test.service.TestService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

    private TestService testService;

//    @GetMapping("/test1")
//    ResultVO<UserDTO> test1(UserDTO data) {
//        TestTool testTool = new TestTool();
//        TestTool.testTool1(data);
//        testTool.testTool2();
//        return new ResultVO<>("0", new UserDTO(), "success");
//    }

    @PostMapping("/test2")
    ResultVO<UserDTO> test2(@RequestBody UserDTO data) {
        if (data == null) {
            return new ResultVO<>("1", new UserDTO(), "failed");
        }
        return new ResultVO<>("0", new UserDTO(), "success");
    }

//    @GetMapping("/test3")
//    ResultVO<UserDO> test3(UserDTO data) {
//        return testService.test(data);
//    }
}