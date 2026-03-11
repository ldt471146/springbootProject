package com.service.impl;

import com.entity.Attachment;
import com.exception.BusinessException;
import com.mapper.AttachmentMapper;
import com.security.UserContext;
import com.service.FileStorageService;
import com.service.support.ViewAssembler;
import com.vo.AttachmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final AttachmentMapper attachmentMapper;
    private final ViewAssembler viewAssembler;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    @Override
    public AttachmentVO store(MultipartFile file, String refType, Long refId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        Set<String> allowSet = Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (extension == null || !allowSet.contains(extension.toLowerCase())) {
            throw new BusinessException(400, "文件类型不支持");
        }
        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path folder = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(dateFolder);
        try {
            Files.createDirectories(folder);
            Path target = folder.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Attachment attachment = new Attachment();
            attachment.setRefType(refType == null ? "COMMON" : refType);
            attachment.setRefId(refId == null ? 0L : refId);
            attachment.setOriginalName(originalFilename);
            attachment.setStoredName(storedName);
            attachment.setFileUrl("/uploads/" + dateFolder + "/" + storedName);
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setCreateBy(UserContext.getCurrentUserId());
            attachmentMapper.insert(attachment);
            return viewAssembler.toAttachmentVO(attachment);
        } catch (IOException ex) {
            throw new BusinessException(500, "文件保存失败");
        }
    }

    @Override
    public Attachment getById(Long id) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null || (attachment.getDeleted() != null && attachment.getDeleted() == 1)) {
            throw new BusinessException(404, "文件不存在");
        }
        return attachment;
    }

    @Override
    public Path resolvePath(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BusinessException(404, "文件不存在");
        }
        String normalized = fileUrl.replace("\\", "/");
        if (normalized.startsWith("/uploads/")) {
            normalized = normalized.substring("/uploads/".length());
        } else if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(normalized).normalize();
    }
}
