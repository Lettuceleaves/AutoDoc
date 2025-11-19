package com.letuc.test.service.Impl;

import com.letuc.test.model.UserDO;
import com.letuc.test.model.UserDTO;
import com.letuc.test.result.ResultVO;
import com.letuc.test.service.TestService;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {
    public ResultVO<UserDO> test(UserDTO userDTO) {
        return new ResultVO<>("test3", new UserDO(userDTO.getUsername(), userDTO.getAge()), "test3");
    }
}
