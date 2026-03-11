package com.dto;

import com.common.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusDTO {

    @NotNull(message = "状态不能为空")
    private UserStatus status;
}
