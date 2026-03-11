package com.service;

public interface ExportService {

    byte[] exportGradesExcel(String semester, String creditType);

    byte[] exportGradesPdf(String semester, String creditType);

    byte[] exportProjectParticipantsExcel(Long projectId);

    byte[] exportProjectParticipantsPdf(Long projectId);
}
