package com.service;

import com.entity.Attachment;
import com.vo.AttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    AttachmentVO store(MultipartFile file, String refType, Long refId);

    Attachment getById(Long id);

    Path resolvePath(String fileUrl);
}
