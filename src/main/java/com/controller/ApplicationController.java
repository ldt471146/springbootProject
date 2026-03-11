package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.ApplicationDTO;
import com.dto.ApplicationPhaseDTO;
import com.dto.ApplicationReviewDTO;
import com.service.ApplicationService;
import com.vo.ApplicationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @RequireRole(RoleEnum.STUDENT)
    @PostMapping
    public Result<ApplicationVO> create(@RequestBody @Valid ApplicationDTO dto) {
        return Result.ok(applicationService.create(dto));
    }

    @RequireRole(RoleEnum.STUDENT)
    @GetMapping("/my")
    public Result<PageResult<ApplicationVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(applicationService.listMyApplications(page, size));
    }

    @GetMapping("/{id}")
    public Result<ApplicationVO> detail(@PathVariable Long id) {
        return Result.ok(applicationService.getDetail(id));
    }

    @RequireRole(RoleEnum.STUDENT)
    @DeleteMapping("/{id}")
    public Result<Void> withdraw(@PathVariable Long id) {
        applicationService.withdraw(id);
        return Result.ok();
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/project/{projectId}")
    public Result<PageResult<ApplicationVO>> projectApplications(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(applicationService.listProjectApplications(projectId, page, size, status));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}/review")
    public Result<ApplicationVO> review(@PathVariable Long id, @RequestBody @Valid ApplicationReviewDTO dto) {
        return Result.ok(applicationService.review(id, dto));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}/phase")
    public Result<ApplicationVO> phase(@PathVariable Long id, @RequestBody @Valid ApplicationPhaseDTO dto) {
        return Result.ok(applicationService.updatePhase(id, dto));
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/teacher/students")
    public Result<PageResult<ApplicationVO>> teacherStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(applicationService.listTeacherStudents(page, size, keyword));
    }
}
