package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.enums.AdminApprovalStatus;
import com.common.enums.ApprovalStatus;
import com.common.enums.ProjectStatus;
import com.common.enums.UserStatus;
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
import com.service.DashboardService;
import com.vo.DashboardSummaryVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final InternshipProjectMapper internshipProjectMapper;
    private final ApplicationMapper applicationMapper;
    private final JournalMapper journalMapper;
    private final ReportMapper reportMapper;
    private final GradeMapper gradeMapper;
    private final UserMapper userMapper;

    public DashboardServiceImpl(
            InternshipProjectMapper internshipProjectMapper,
            ApplicationMapper applicationMapper,
            JournalMapper journalMapper,
            ReportMapper reportMapper,
            GradeMapper gradeMapper,
            UserMapper userMapper
    ) {
        this.internshipProjectMapper = internshipProjectMapper;
        this.applicationMapper = applicationMapper;
        this.journalMapper = journalMapper;
        this.reportMapper = reportMapper;
        this.gradeMapper = gradeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public DashboardSummaryVO currentUserSummary() {
        DashboardSummaryVO summary = new DashboardSummaryVO();
        String role = UserContext.getCurrentRole();
        Long userId = UserContext.getCurrentUserId();
        summary.setRole(role);
        if (role == null || userId == null) {
            return summary;
        }
        if ("STUDENT".equals(role)) {
            fillStudentSummary(summary, userId);
            return summary;
        }
        if ("TEACHER".equals(role)) {
            fillTeacherSummary(summary, userId);
            return summary;
        }
        if ("ADMIN".equals(role)) {
            fillAdminSummary(summary);
        }
        return summary;
    }

    private void fillStudentSummary(DashboardSummaryVO summary, Long studentId) {
        summary.setStudentOpenProjects(countProjectsByStatus(ProjectStatus.OPEN.name()));
        summary.setStudentApplications(countApplicationsByStudent(studentId));
        summary.setStudentApprovedApplications(countApplicationsByStudentAndStatus(studentId, ApprovalStatus.APPROVED.name()));
        summary.setStudentJournals(countJournalsByStudent(studentId));
        summary.setStudentReports(countReportsByStudent(studentId));
        summary.setStudentConfirmedGrades(countGradesByStudentAndAdminStatus(studentId, AdminApprovalStatus.CONFIRMED.name()));
    }

    private void fillTeacherSummary(DashboardSummaryVO summary, Long teacherId) {
        summary.setTeacherProjects(countTeacherProjects(teacherId));
        Set<Long> projectIds = findTeacherProjectIds(teacherId);
        if (projectIds.isEmpty()) {
            return;
        }
        summary.setTeacherPendingApplications(countApplicationsByProjectIdsAndStatus(projectIds, ApprovalStatus.PENDING.name()));
        Set<Long> approvedApplicationIds = findApplicationIdsByProjectIdsAndStatus(projectIds, ApprovalStatus.APPROVED.name());
        summary.setTeacherActiveStudents(approvedApplicationIds.size());
        if (approvedApplicationIds.isEmpty()) {
            return;
        }
        summary.setTeacherPendingJournals(countTeacherPendingJournals(approvedApplicationIds));
        summary.setTeacherPendingReports(countTeacherPendingReports(approvedApplicationIds));
    }

    private void fillAdminSummary(DashboardSummaryVO summary) {
        summary.setAdminPendingUsers(countUsersByStatus(UserStatus.PENDING.name()));
        summary.setAdminPendingGrades(countGradesByAdminStatus(AdminApprovalStatus.PENDING.name()));
        summary.setAdminActiveTeachers(countUsersByRoleAndStatus("TEACHER", UserStatus.ACTIVE.name()));
        summary.setAdminActiveStudents(countUsersByRoleAndStatus("STUDENT", UserStatus.ACTIVE.name()));
        summary.setAdminOpenProjects(countProjectsByStatus(ProjectStatus.OPEN.name()));
    }

    private long countProjectsByStatus(String status) {
        Long count = internshipProjectMapper.selectCount(new LambdaQueryWrapper<InternshipProject>()
                .eq(InternshipProject::getStatus, status));
        return safeCount(count);
    }

    private long countTeacherProjects(Long teacherId) {
        Long count = internshipProjectMapper.selectCount(new LambdaQueryWrapper<InternshipProject>()
                .eq(InternshipProject::getTeacherId, teacherId));
        return safeCount(count);
    }

    private long countApplicationsByStudent(Long studentId) {
        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getStudentId, studentId));
        return safeCount(count);
    }

    private long countApplicationsByStudentAndStatus(Long studentId, String status) {
        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getStudentId, studentId)
                .eq(InternApplication::getStatus, status));
        return safeCount(count);
    }

    private long countApplicationsByProjectIdsAndStatus(Set<Long> projectIds, String status) {
        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<InternApplication>()
                .in(InternApplication::getProjectId, projectIds)
                .eq(InternApplication::getStatus, status));
        return safeCount(count);
    }

    private Set<Long> findTeacherProjectIds(Long teacherId) {
        List<InternshipProject> projects = internshipProjectMapper.selectList(new LambdaQueryWrapper<InternshipProject>()
                .eq(InternshipProject::getTeacherId, teacherId));
        if (projects.isEmpty()) {
            return Collections.emptySet();
        }
        return projects.stream().map(InternshipProject::getId).collect(Collectors.toSet());
    }

    private Set<Long> findApplicationIdsByProjectIdsAndStatus(Set<Long> projectIds, String status) {
        List<InternApplication> applications = applicationMapper.selectList(new LambdaQueryWrapper<InternApplication>()
                .in(InternApplication::getProjectId, projectIds)
                .eq(InternApplication::getStatus, status));
        if (applications.isEmpty()) {
            return Collections.emptySet();
        }
        return applications.stream().map(InternApplication::getId).collect(Collectors.toSet());
    }

    private long countJournalsByStudent(Long studentId) {
        Long count = journalMapper.selectCount(new LambdaQueryWrapper<Journal>()
                .eq(Journal::getStudentId, studentId));
        return safeCount(count);
    }

    private long countTeacherPendingJournals(Set<Long> applicationIds) {
        Long count = journalMapper.selectCount(new LambdaQueryWrapper<Journal>()
                .in(Journal::getApplicationId, applicationIds)
                .isNull(Journal::getScore));
        return safeCount(count);
    }

    private long countReportsByStudent(Long studentId) {
        Long count = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getStudentId, studentId));
        return safeCount(count);
    }

    private long countTeacherPendingReports(Set<Long> applicationIds) {
        Long count = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .in(Report::getApplicationId, applicationIds)
                .eq(Report::getStatus, com.common.enums.ReportStatus.SUBMITTED.name()));
        return safeCount(count);
    }

    private long countGradesByStudentAndAdminStatus(Long studentId, String adminStatus) {
        Long count = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId)
                .eq(Grade::getAdminStatus, adminStatus));
        return safeCount(count);
    }

    private long countGradesByAdminStatus(String adminStatus) {
        Long count = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getAdminStatus, adminStatus));
        return safeCount(count);
    }

    private long countUsersByStatus(String status) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, status));
        return safeCount(count);
    }

    private long countUsersByRoleAndStatus(String role, String status) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, role)
                .eq(User::getStatus, status));
        return safeCount(count);
    }

    private long safeCount(Long count) {
        return count == null ? 0 : count;
    }
}
