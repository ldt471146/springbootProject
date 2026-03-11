package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("intern_project")
public class InternshipProject {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long teacherId;
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
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
