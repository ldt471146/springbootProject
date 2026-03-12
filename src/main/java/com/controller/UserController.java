package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.ApprovalDTO;
import com.dto.PasswordDTO;
import com.dto.PasswordResetDTO;
import com.dto.UserProfileDTO;
import com.dto.UserStatusDTO;
import com.service.UserService;
import com.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody @Valid UserProfileDTO dto) {
        return Result.ok(userService.updateProfile(dto));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid PasswordDTO dto) {
        userService.changePassword(dto);
        return Result.ok();
    }

    @RequireRole(RoleEnum.ADMIN)
    @PutMapping("/password/reset/{id}")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid PasswordResetDTO dto) {
        userService.resetPassword(id, dto);
        return Result.ok();
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/pending")
    public Result<PageResult<UserVO>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.listPending(page, size, keyword));
    }

    @RequireRole(RoleEnum.ADMIN)
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody @Valid ApprovalDTO dto) {
        userService.approve(id, dto);
        return Result.ok();
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return Result.ok(userService.listUsers(page, size, keyword, role, status));
    }

    @RequireRole(RoleEnum.ADMIN)
    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @RequestBody @Valid UserStatusDTO dto) {
        userService.changeStatus(id, dto);
        return Result.ok();
    }

    @RequireRole(RoleEnum.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }
}
