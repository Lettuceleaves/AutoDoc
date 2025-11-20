package com.letuc.test.controller;

import com.letuc.test.model.UserDO;
import com.letuc.test.model.UserDTO;
import com.letuc.test.result.Result;
import com.letuc.test.result.ResultVO;
import com.letuc.test.result.errorcode.ContestErrorCode;
import com.letuc.test.service.TestService;
import com.letuc.test.tool.TestTool;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

    private TestService testService;

    @GetMapping("/test1")
    ResultVO<UserDTO> test1(UserDTO data) {
        TestTool testTool = new TestTool();
        TestTool.testTool1(data);
        testTool.testTool2();
        return new ResultVO<>("0", new UserDTO(), "success");
    }

    @PostMapping("/test2")
    ResultVO<UserDTO> test2(@RequestBody UserDTO data) {
        if (data == null) {
            return new ResultVO<>("1", new UserDTO(), "failed");
        } else if (data.getAge() == 1) {
            return Result.failure(ContestErrorCode.CONTEST_FINISHED);
        }
        return Result.success();
    }

     @GetMapping("/hello")
     String hello() {
         return "hello";
     }

    @GetMapping("/test3")
    ResultVO<UserDO> test3(UserDTO data) {
        return testService.test(data);
    }
}