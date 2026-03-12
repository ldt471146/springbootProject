package com.controller;

import com.annotation.RequireRole;
import com.common.PageResult;
import com.common.Result;
import com.common.enums.RoleEnum;
import com.dto.JournalDTO;
import com.dto.JournalReviewDTO;
import com.entity.Attachment;
import com.service.DocumentPreviewService;
import com.service.FileStorageService;
import com.service.JournalService;
import com.vo.AttachmentVO;
import com.vo.FilePreviewVO;
import com.vo.JournalVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;
    private final FileStorageService fileStorageService;
    private final DocumentPreviewService documentPreviewService;

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

    @RequireRole(RoleEnum.STUDENT)
    @PostMapping("/{id}/attachments")
    public Result<AttachmentVO> uploadAttachment(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return Result.ok(journalService.uploadAttachment(id, file));
    }

    @RequireRole(RoleEnum.STUDENT)
    @DeleteMapping("/attachments/{attachmentId}")
    public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
        journalService.deleteAttachment(attachmentId);
        return Result.ok();
    }

    @GetMapping("/attachments/{attachmentId}/file")
    public ResponseEntity<Resource> attachmentFile(@PathVariable Long attachmentId) throws IOException {
        Attachment attachment = journalService.getAttachmentByIdOrThrow(attachmentId);
        Path path = fileStorageService.resolvePath(attachment.getFileUrl());
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        boolean inline = contentType != null && (contentType.startsWith("image/") || "application/pdf".equalsIgnoreCase(contentType));
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder(inline ? "inline" : "attachment")
                        .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }

    @GetMapping("/attachments/{attachmentId}/preview")
    public Result<FilePreviewVO> attachmentPreview(@PathVariable Long attachmentId) throws IOException {
        Attachment attachment = journalService.getAttachmentByIdOrThrow(attachmentId);
        Path path = fileStorageService.resolvePath(attachment.getFileUrl());
        String contentType = Files.probeContentType(path);
        return Result.ok(documentPreviewService.buildPreview(path, attachment.getOriginalName(), contentType));
    }
}
