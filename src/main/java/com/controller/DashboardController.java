package com.controller;

import com.common.Result;
import com.service.DashboardService;
import com.vo.DashboardSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardSummaryVO> summary() {
        return Result.ok(dashboardService.currentUserSummary());
    }
}
