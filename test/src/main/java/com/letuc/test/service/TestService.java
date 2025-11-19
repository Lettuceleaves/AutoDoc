package com.letuc.test.service;

import com.letuc.test.model.UserDO;
import com.letuc.test.model.UserDTO;
import com.letuc.test.result.ResultVO;

public interface TestService {
    ResultVO<UserDO> test(UserDTO userDTO);
}
