package com.controller;

import com.common.Result;
import com.entity.Attachment;
import com.service.FileStorageService;
import com.vo.AttachmentVO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result<AttachmentVO> upload(@RequestPart("file") MultipartFile file) {
        return Result.ok(fileStorageService.store(file, "COMMON", 0L));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        return buildFileResponse(id, false);
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> preview(@PathVariable Long id) throws IOException {
        return buildFileResponse(id, true);
    }

    private ResponseEntity<Resource> buildFileResponse(Long id, boolean inline) throws IOException {
        Attachment attachment = fileStorageService.getById(id);
        Path path = fileStorageService.resolvePath(attachment.getFileUrl());
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder(inline ? "inline" : "attachment")
                        .filename(attachment.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }
}
