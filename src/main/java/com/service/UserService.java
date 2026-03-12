package com.service;

import com.common.PageResult;
import com.dto.ApprovalDTO;
import com.dto.PasswordDTO;
import com.dto.PasswordResetDTO;
import com.dto.UserProfileDTO;
import com.dto.UserStatusDTO;
import com.entity.User;
import com.vo.UserVO;

public interface UserService {

    UserVO updateProfile(UserProfileDTO dto);

    void changePassword(PasswordDTO dto);

    void resetPassword(Long userId, PasswordResetDTO dto);

    PageResult<UserVO> listPending(int page, int size, String keyword);

    void approve(Long userId, ApprovalDTO dto);

    PageResult<UserVO> listUsers(int page, int size, String keyword, String role, String status);

    void changeStatus(Long userId, UserStatusDTO dto);

    void deleteUser(Long userId);

    User getByIdOrThrow(Long id);

    User getByIdIncludingDeletedOrThrow(Long id);

    UserVO getCurrentUser();
}
