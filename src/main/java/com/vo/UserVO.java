package com.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private String status;
    private String studentNo;
    private String college;
    private String major;
    private String grade;
    private String className;
    private String phone;
    private String email;
    private String avatar;
    private LocalDateTime createTime;
}
