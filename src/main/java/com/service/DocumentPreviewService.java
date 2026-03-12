package com.service;

import com.vo.FilePreviewVO;

import java.io.IOException;
import java.nio.file.Path;

public interface DocumentPreviewService {

    FilePreviewVO buildPreview(Path path, String fileName, String contentType) throws IOException;
}
