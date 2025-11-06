package com.letuc.test.model;

import lombok.Data;

@Data
public class UserDTO {
    String username;
    String password;
    Integer age;
    int status;
    char sex;
    boolean admin;
    byte[] photo;
}
