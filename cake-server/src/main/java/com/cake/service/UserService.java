package com.cake.service;

import com.cake.dto.UserLoginDTO;
import com.cake.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    /**
     * 微信用户的登录
     * @param userLoginDTO
     * @return
     */
    User wxlogin(UserLoginDTO userLoginDTO);
}
