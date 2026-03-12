package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.enums.AdminApprovalStatus;
import com.common.enums.ApprovalStatus;
import com.common.enums.ReportStatus;
import com.entity.Grade;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.entity.Journal;
import com.entity.Report;
import com.entity.User;
import com.mapper.ApplicationMapper;
import com.mapper.GradeMapper;
import com.mapper.InternshipProjectMapper;
import com.mapper.JournalMapper;
import com.mapper.ReportMapper;
import com.mapper.UserMapper;
import com.security.UserContext;
import com.service.ReminderService;
import com.vo.ReminderSummaryVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final UserMapper userMapper;
    private final GradeMapper gradeMapper;
    private final InternshipProjectMapper internshipProjectMapper;
    private final ApplicationMapper applicationMapper;
    private final JournalMapper journalMapper;
    private final ReportMapper reportMapper;

    public ReminderServiceImpl(
            UserMapper userMapper,
            GradeMapper gradeMapper,
            InternshipProjectMapper internshipProjectMapper,
            ApplicationMapper applicationMapper,
            JournalMapper journalMapper,
            ReportMapper reportMapper
    ) {
        this.userMapper = userMapper;
        this.gradeMapper = gradeMapper;
        this.internshipProjectMapper = internshipProjectMapper;
        this.applicationMapper = applicationMapper;
        this.journalMapper = journalMapper;
        this.reportMapper = reportMapper;
    }

    @Override
    public ReminderSummaryVO currentUserSummary() {
        ReminderSummaryVO summary = new ReminderSummaryVO(0, 0, 0, 0, 0);
        String role = UserContext.getCurrentRole();
        Long userId = UserContext.getCurrentUserId();
        if (userId == null || role == null) {
            return summary;
        }
        if ("ADMIN".equals(role)) {
            summary.setAdminPendingUsers(countAdminPendingUsers());
            summary.setAdminPendingGrades(countAdminPendingGrades());
            return summary;
        }
        if ("TEACHER".equals(role)) {
            Set<Long> projectIds = findTeacherProjectIds(userId);
            if (projectIds.isEmpty()) {
                return summary;
            }
            summary.setTeacherPendingApplications(countTeacherPendingApplications(projectIds));
            Set<Long> approvedApplicationIds = findApprovedApplicationIds(projectIds);
            if (approvedApplicationIds.isEmpty()) {
                return summary;
            }
            summary.setTeacherPendingJournals(countTeacherPendingJournals(approvedApplicationIds));
            summary.setTeacherPendingReports(countTeacherPendingReports(approvedApplicationIds));
        }
        return summary;
    }

    private long countAdminPendingUsers() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, ApprovalStatus.PENDING.name()));
        return count == null ? 0 : count;
    }

    private long countAdminPendingGrades() {
        Long count = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getAdminStatus, AdminApprovalStatus.PENDING.name()));
        return count == null ? 0 : count;
    }

    private Set<Long> findTeacherProjectIds(Long teacherId) {
        List<InternshipProject> projects = internshipProjectMapper.selectList(new LambdaQueryWrapper<InternshipProject>()
                .eq(InternshipProject::getTeacherId, teacherId));
        if (projects.isEmpty()) {
            return Collections.emptySet();
        }
        return projects.stream().map(InternshipProject::getId).collect(Collectors.toSet());
    }

    private long countTeacherPendingApplications(Set<Long> projectIds) {
        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<InternApplication>()
                .in(InternApplication::getProjectId, projectIds)
                .eq(InternApplication::getStatus, ApprovalStatus.PENDING.name()));
        return count == null ? 0 : count;
    }

    private Set<Long> findApprovedApplicationIds(Set<Long> projectIds) {
        List<InternApplication> applications = applicationMapper.selectList(new LambdaQueryWrapper<InternApplication>()
                .in(InternApplication::getProjectId, projectIds)
                .eq(InternApplication::getStatus, ApprovalStatus.APPROVED.name()));
        if (applications.isEmpty()) {
            return Collections.emptySet();
        }
        return applications.stream().map(InternApplication::getId).collect(Collectors.toSet());
    }

    private long countTeacherPendingJournals(Set<Long> applicationIds) {
        Long count = journalMapper.selectCount(new LambdaQueryWrapper<Journal>()
                .in(Journal::getApplicationId, applicationIds)
                .isNull(Journal::getScore));
        return count == null ? 0 : count;
    }

    private long countTeacherPendingReports(Set<Long> applicationIds) {
        Long count = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .in(Report::getApplicationId, applicationIds)
                .eq(Report::getStatus, ReportStatus.SUBMITTED.name()));
        return count == null ? 0 : count;
    }
}
