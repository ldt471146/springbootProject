package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.JournalDTO;
import com.dto.JournalReviewDTO;
import com.service.JournalService;
import com.vo.JournalVO;
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

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @RequireRole(RoleEnum.STUDENT)
    @PostMapping
    public Result<JournalVO> create(@RequestBody @Valid JournalDTO dto) {
        return Result.ok(journalService.create(dto));
    }

    @RequireRole(RoleEnum.STUDENT)
    @PutMapping("/{id}")
    public Result<JournalVO> update(@PathVariable Long id, @RequestBody @Valid JournalDTO dto) {
        return Result.ok(journalService.update(id, dto));
    }

    @RequireRole(RoleEnum.STUDENT)
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        journalService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<JournalVO> detail(@PathVariable Long id) {
        return Result.ok(journalService.getDetail(id));
    }

    @RequireRole(RoleEnum.STUDENT)
    @GetMapping("/my")
    public Result<PageResult<JournalVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(journalService.listMy(page, size));
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/application/{appId}")
    public Result<PageResult<JournalVO>> listByApplication(
            @PathVariable Long appId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(journalService.listByApplication(appId, page, size));
    }

    @GetMapping("/timeline/{appId}")
    public Result<List<JournalVO>> timeline(@PathVariable Long appId) {
        return Result.ok(journalService.timeline(appId));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}/review")
    public Result<JournalVO> review(@PathVariable Long id, @RequestBody @Valid JournalReviewDTO dto) {
        return Result.ok(journalService.review(id, dto));
    }
}
