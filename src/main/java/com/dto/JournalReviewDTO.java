package com.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalReviewDTO {

    private String teacherComment;
    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "100", message = "评分不能大于100")
    private BigDecimal score;
}
