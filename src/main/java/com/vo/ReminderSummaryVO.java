package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderSummaryVO {

    private long adminPendingUsers;
    private long adminPendingGrades;
    private long teacherPendingApplications;
    private long teacherPendingJournals;
    private long teacherPendingReports;
}
