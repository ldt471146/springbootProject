package com.dto;

import com.common.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "角色不能为空")
    private RoleEnum role;

    private String studentNo;
    private String college;
    private String major;
    private String grade;
    private String className;
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
