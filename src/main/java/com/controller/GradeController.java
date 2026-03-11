package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.GradeConfirmDTO;
import com.dto.GradeDTO;
import com.service.GradeService;
import com.vo.GradeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grade")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @RequireRole(RoleEnum.TEACHER)
    @PostMapping
    public Result<GradeVO> create(@RequestBody @Valid GradeDTO dto) {
        return Result.ok(gradeService.create(dto));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}")
    public Result<GradeVO> update(@PathVariable Long id, @RequestBody @Valid GradeDTO dto) {
        return Result.ok(gradeService.update(id, dto));
    }

    @GetMapping("/{id}")
    public Result<GradeVO> detail(@PathVariable Long id) {
        return Result.ok(gradeService.getDetail(id));
    }

    @RequireRole(RoleEnum.STUDENT)
    @GetMapping("/my")
    public Result<PageResult<GradeVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(gradeService.listMy(page, size));
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/teacher/list")
    public Result<PageResult<GradeVO>> teacherList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(gradeService.listTeacher(page, size));
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/admin/list")
    public Result<PageResult<GradeVO>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String creditType) {
        return Result.ok(gradeService.listAdmin(page, size, status, semester, creditType));
    }

    @RequireRole(RoleEnum.ADMIN)
    @PutMapping("/{id}/confirm")
    public Result<GradeVO> confirm(@PathVariable Long id, @RequestBody @Valid GradeConfirmDTO dto) {
        return Result.ok(gradeService.confirm(id, dto));
    }
}
