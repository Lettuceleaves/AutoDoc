package com.letuc.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDO {
    private String name;
    private Integer age;
}
