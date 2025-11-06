package com.letuc.test.controller;

import com.letuc.test.model.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/hello")
    String test(UserDTO data) {return "hello";}
}
