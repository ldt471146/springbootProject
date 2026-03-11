package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.ProjectDTO;
import com.service.InternshipProjectService;
import com.vo.ProjectVO;
import com.vo.UserVO;
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
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class InternshipProjectController {

    private final InternshipProjectService projectService;

    @RequireRole(RoleEnum.TEACHER)
    @PostMapping
    public Result<ProjectVO> create(@RequestBody @Valid ProjectDTO dto) {
        return Result.ok(projectService.create(dto));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}")
    public Result<ProjectVO> update(@PathVariable Long id, @RequestBody @Valid ProjectDTO dto) {
        return Result.ok(projectService.update(id, dto));
    }

    @RequireRole(RoleEnum.TEACHER)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> detail(@PathVariable Long id) {
        return Result.ok(projectService.getDetail(id));
    }

    @RequireRole(RoleEnum.STUDENT)
    @GetMapping("/list")
    public Result<PageResult<ProjectVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String grade) {
        return Result.ok(projectService.listOpenProjects(page, size, keyword, college, grade));
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/my")
    public Result<PageResult<ProjectVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(projectService.listMyProjects(page, size));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        projectService.close(id);
        return Result.ok();
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        projectService.archive(id);
        return Result.ok();
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/{id}/participants")
    public Result<PageResult<UserVO>> participants(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(projectService.listParticipants(id, page, size));
    }
}
