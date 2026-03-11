package com.dto;

import com.common.enums.JournalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JournalDTO {

    @NotNull(message = "申请不能为空")
    private Long applicationId;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    @NotNull(message = "日志类型不能为空")
    private JournalType journalType;
    @NotNull(message = "日志日期不能为空")
    private LocalDate journalDate;
    private Integer weekNo;
}
