package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.enums.RoleEnum;
import com.common.enums.UserStatus;
import com.dto.LoginDTO;
import com.dto.RegisterDTO;
import com.entity.User;
import com.exception.BusinessException;
import com.mapper.UserMapper;
import com.security.JwtUtil;
import com.security.UserContext;
import com.service.AuthService;
import com.service.support.ViewAssembler;
import com.vo.LoginVO;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ViewAssembler viewAssembler;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getDeleted, 0)
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败: username={}, reason=invalid_credentials", dto.getUsername());
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (UserStatus.PENDING.name().equals(user.getStatus())) {
            log.warn("登录失败: userId={}, username={}, reason=pending", user.getId(), user.getUsername());
            throw new BusinessException(403, "账号待审批");
        }
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            log.warn("登录失败: userId={}, username={}, reason=disabled", user.getId(), user.getUsername());
            throw new BusinessException(403, "账号已禁用");
        }
        log.info("登录成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(jwtUtil.generateToken(user.getId(), user.getRole()), viewAssembler.toUserVO(user));
    }

    @Override
    public void register(RegisterDTO dto) {
        if (dto.getRole() == RoleEnum.ADMIN) {
            throw new BusinessException(400, "管理员账号不允许自行注册");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole().name());
        user.setStatus(UserStatus.PENDING.name());
        user.setStudentNo(dto.getStudentNo());
        user.setCollege(dto.getCollege());
        user.setMajor(dto.getMajor());
        user.setGrade(dto.getGrade());
        user.setClassName(dto.getClassName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userMapper.insert(user);
        log.info("注册提交成功: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public UserVO currentUser() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        log.info("获取当前用户信息: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return viewAssembler.toUserVO(user);
    }
}
