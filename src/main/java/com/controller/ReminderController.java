package com.controller;

import com.common.Result;
import com.service.ReminderService;
import com.vo.ReminderSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/me")
    public Result<ReminderSummaryVO> me() {
        return Result.ok(reminderService.currentUserSummary());
    }
}
