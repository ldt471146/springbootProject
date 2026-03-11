package com.controller;

import com.annotation.RequireRole;
import com.common.enums.RoleEnum;
import com.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/grades")
    public ResponseEntity<byte[]> exportGrades(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String creditType) {
        return buildBinaryResponse(exportService.exportGradesExcel(semester, creditType),
                "grades.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @RequireRole(RoleEnum.ADMIN)
    @GetMapping("/grades/pdf")
    public ResponseEntity<byte[]> exportGradesPdf(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String creditType) {
        return buildBinaryResponse(exportService.exportGradesPdf(semester, creditType), "grades.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/project/{id}/participants")
    public ResponseEntity<byte[]> exportProjectParticipants(@PathVariable Long id) {
        return buildBinaryResponse(exportService.exportProjectParticipantsExcel(id),
                "participants.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/project/{id}/participants/pdf")
    public ResponseEntity<byte[]> exportProjectParticipantsPdf(@PathVariable Long id) {
        return buildBinaryResponse(exportService.exportProjectParticipantsPdf(id), "participants.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private ResponseEntity<byte[]> buildBinaryResponse(byte[] bytes, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
