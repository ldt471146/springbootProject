package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.ReportDTO;
import com.dto.ReportReviewDTO;
import com.entity.Report;
import com.service.FileStorageService;
import com.service.ReportService;
import com.vo.AttachmentVO;
import com.vo.ReportVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final FileStorageService fileStorageService;

    @RequireRole(RoleEnum.STUDENT)
    @PostMapping
    public Result<ReportVO> create(@RequestBody @Valid ReportDTO dto) {
        return Result.ok(reportService.create(dto));
    }

    @RequireRole(RoleEnum.STUDENT)
    @PutMapping("/{id}")
    public Result<ReportVO> update(@PathVariable Long id, @RequestBody @Valid ReportDTO dto) {
        return Result.ok(reportService.update(id, dto));
    }

    @GetMapping("/{id}")
    public Result<ReportVO> detail(@PathVariable Long id) {
        return Result.ok(reportService.getDetail(id));
    }

    @RequireRole(RoleEnum.STUDENT)
    @GetMapping("/my")
    public Result<PageResult<ReportVO>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(reportService.listMy(page, size));
    }

    @RequireRole(RoleEnum.TEACHER)
    @GetMapping("/teacher/list")
    public Result<PageResult<ReportVO>> teacherList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(reportService.teacherList(page, size, status));
    }

    @RequireRole(RoleEnum.TEACHER)
    @PutMapping("/{id}/review")
    public Result<ReportVO> review(@PathVariable Long id, @RequestBody @Valid ReportReviewDTO dto) {
        return Result.ok(reportService.review(id, dto));
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> file(@PathVariable Long id) throws IOException {
        Report report = reportService.getByIdOrThrow(id);
        Path path = fileStorageService.resolvePath(report.getFileUrl());
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        boolean inline = "application/pdf".equalsIgnoreCase(contentType);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder(inline ? "inline" : "attachment")
                        .filename(path.getFileName().toString(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }

    @RequireRole(RoleEnum.STUDENT)
    @PostMapping("/upload")
    public Result<AttachmentVO> upload(@RequestPart("file") MultipartFile file) {
        return Result.ok(reportService.upload(file));
    }
}
