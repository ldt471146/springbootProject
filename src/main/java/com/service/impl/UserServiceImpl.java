package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.PageResult;
import com.common.enums.ApprovalStatus;
import com.common.enums.RoleEnum;
import com.common.enums.UserStatus;
import com.dto.ApprovalDTO;
import com.dto.PasswordDTO;
import com.dto.PasswordResetDTO;
import com.dto.UserProfileDTO;
import com.dto.UserStatusDTO;
import com.entity.User;
import com.exception.BusinessException;
import com.mapper.UserMapper;
import com.security.UserContext;
import com.service.UserService;
import com.service.support.ViewAssembler;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ViewAssembler viewAssembler;

    @Override
    public UserVO updateProfile(UserProfileDTO dto) {
        User user = getByIdOrThrow(UserContext.getCurrentUserId());
        user.setRealName(dto.getRealName());
        user.setCollege(dto.getCollege());
        user.setMajor(dto.getMajor());
        user.setGrade(dto.getGrade());
        user.setClassName(dto.getClassName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        return viewAssembler.toUserVO(user);
    }

    @Override
    public void changePassword(PasswordDTO dto) {
        User user = getByIdOrThrow(UserContext.getCurrentUserId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, PasswordResetDTO dto) {
        User user = getByIdOrThrow(userId);
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public PageResult<UserVO> listPending(int page, int size, String keyword) {
        Page<User> pager = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getStatus, UserStatus.PENDING.name())
                .orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        }
        Page<User> result = userMapper.selectPage(pager, wrapper);
        List<UserVO> records = result.getRecords().stream().map(viewAssembler::toUserVO).toList();
        return PageResult.of(result, records);
    }

    @Override
    public void approve(Long userId, ApprovalDTO dto) {
        User user = getByIdOrThrow(userId);
        if (RoleEnum.ADMIN.name().equals(user.getRole()) && ApprovalStatus.REJECTED == dto.getStatus()) {
            throw new BusinessException(400, "不能驳回管理员账号");
        }
        user.setStatus(dto.getStatus() == ApprovalStatus.APPROVED
                ? UserStatus.ACTIVE.name()
                : UserStatus.DISABLED.name());
        userMapper.updateById(user);
    }

    @Override
    public PageResult<UserVO> listUsers(int page, int size, String keyword, String role, String status) {
        Page<User> pager = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(User::getStatus, status);
        }
        Page<User> result = userMapper.selectPage(pager, wrapper);
        List<UserVO> records = result.getRecords().stream().map(viewAssembler::toUserVO).toList();
        return PageResult.of(result, records);
    }

    @Override
    public void changeStatus(Long userId, UserStatusDTO dto) {
        if (userId.equals(UserContext.getCurrentUserId()) && UserStatus.DISABLED == dto.getStatus()) {
            throw new BusinessException(400, "不能禁用当前登录账号");
        }
        User user = getByIdOrThrow(userId);
        user.setStatus(dto.getStatus().name());
        userMapper.updateById(user);
    }

    @Override
    public User getByIdOrThrow(Long id) {
        if (id == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = userMapper.selectById(id);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public UserVO getCurrentUser() {
        return viewAssembler.toUserVO(getByIdOrThrow(UserContext.getCurrentUserId()));
    }
}
