package com.dto;

import com.common.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectDTO {

    @NotBlank(message = "项目标题不能为空")
    private String title;
    private String description;
    private String company;
    private String location;
    private Integer maxStudents;
    private String collegeLimit;
    private String gradeLimit;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate enrollDeadline;
    private String semester;
    private String creditType;
    private ProjectStatus status;
}
