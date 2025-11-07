package com.letuc.test.controller;

import com.letuc.test.model.UserDTO;
import com.letuc.test.result.ResultVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
//    @GetMapping("/test1")
//    ResultVO<UserDTO> test1(UserDTO data) {
//        return new ResultVO<>("0", new UserDTO(), "success");
//    }

    @PostMapping("/test2")
    ResultVO test2(UserDTO data) {
        return new ResultVO<>("0", new UserDTO(), "success");
    }
}