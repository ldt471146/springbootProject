package com.service;

import com.dto.LoginDTO;
import com.dto.RegisterDTO;
import com.vo.LoginVO;
import com.vo.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    UserVO currentUser();
}
