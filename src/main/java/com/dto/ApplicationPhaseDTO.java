package com.dto;

import com.common.enums.InternshipPhase;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationPhaseDTO {

    @NotNull(message = "阶段不能为空")
    private InternshipPhase phase;
}
