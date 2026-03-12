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
import com.entity.InternshipProject;
import com.entity.User;
import com.exception.BusinessException;
import com.mapper.InternshipProjectMapper;
import com.mapper.UserMapper;
import com.security.UserContext;
import com.service.UserService;
import com.service.support.ViewAssembler;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final InternshipProjectMapper projectMapper;
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
        log.info("个人资料已更新: userId={}, username={}", user.getId(), user.getUsername());
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
        log.info("用户已修改密码: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public void resetPassword(Long userId, PasswordResetDTO dto) {
        User user = getByIdOrThrow(userId);
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        log.info("管理员重置密码: targetUserId={}, username={}, operatorId={}",
                user.getId(), user.getUsername(), UserContext.getCurrentUserId());
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
        log.info("用户审批完成: targetUserId={}, username={}, result={}, operatorId={}",
                user.getId(), user.getUsername(), user.getStatus(), UserContext.getCurrentUserId());
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
        log.info("用户状态已更新: targetUserId={}, username={}, newStatus={}, operatorId={}",
                user.getId(), user.getUsername(), user.getStatus(), UserContext.getCurrentUserId());
    }

    @Override
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (userId.equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(400, "不能删除当前登录账号");
        }
        User user = getByIdOrThrow(userId);
        if (RoleEnum.ADMIN.name().equals(user.getRole())) {
            throw new BusinessException(400, "不能删除管理员账号");
        }
        if (RoleEnum.TEACHER.name().equals(user.getRole())) {
            Long projectCount = projectMapper.selectCount(new LambdaQueryWrapper<InternshipProject>()
                    .eq(InternshipProject::getTeacherId, userId));
            if (projectCount != null && projectCount > 0) {
                throw new BusinessException(400, "该教师名下仍有关联项目，不能直接删除");
            }
        }
        userMapper.deleteById(userId);
        log.info("用户已删除: targetUserId={}, username={}, operatorId={}",
                user.getId(), user.getUsername(), UserContext.getCurrentUserId());
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
    public User getByIdIncludingDeletedOrThrow(Long id) {
        if (id == null) {
            throw new BusinessException(404, "用户不存在");
        }
        User user = userMapper.selectIncludingDeletedById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public UserVO getCurrentUser() {
        return viewAssembler.toUserVO(getByIdOrThrow(UserContext.getCurrentUserId()));
    }
}
