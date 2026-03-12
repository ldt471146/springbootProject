package com.vo;

import lombok.Data;

@Data
public class DashboardSummaryVO {

    private String role;

    private long studentOpenProjects;
    private long studentApplications;
    private long studentApprovedApplications;
    private long studentJournals;
    private long studentReports;
    private long studentConfirmedGrades;

    private long teacherProjects;
    private long teacherPendingApplications;
    private long teacherPendingJournals;
    private long teacherPendingReports;
    private long teacherActiveStudents;

    private long adminPendingUsers;
    private long adminPendingGrades;
    private long adminActiveTeachers;
    private long adminActiveStudents;
    private long adminOpenProjects;
}
