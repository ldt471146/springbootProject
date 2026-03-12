package com.service.impl;

import com.service.DocumentPreviewService;
import com.vo.FilePreviewVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@Service
public class DocumentPreviewServiceImpl implements DocumentPreviewService {

    @Override
    public FilePreviewVO buildPreview(Path path, String fileName, String contentType) throws IOException {
        String resolvedFileName = fileName == null || fileName.isBlank()
                ? path.getFileName().toString()
                : fileName;
        String resolvedContentType = contentType == null || contentType.isBlank()
                ? Files.probeContentType(path)
                : contentType;
        String extension = getExtension(resolvedFileName);
        FilePreviewVO preview = new FilePreviewVO();
        preview.setFileName(resolvedFileName);
        preview.setContentType(resolvedContentType);
        if ("pdf".equals(extension) || "application/pdf".equalsIgnoreCase(resolvedContentType)) {
            preview.setMode("pdf");
            return preview;
        }
        if (resolvedContentType != null && resolvedContentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            preview.setMode("image");
            return preview;
        }
        if ("docx".equals(extension)) {
            preview.setMode("docx");
            preview.setHtmlContent(renderDocxAsHtml(path, resolvedFileName));
            return preview;
        }
        preview.setMode("unsupported");
        preview.setMessage("当前文件类型暂不支持在线预览，请下载后查看");
        return preview;
    }

    private String renderDocxAsHtml(Path path, String fileName) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder html = new StringBuilder();
            html.append("<section class=\"docx-preview-doc\">");
            html.append("<header class=\"docx-preview-head\">");
            html.append("<h1>").append(HtmlUtils.htmlEscape(fileName)).append("</h1>");
            html.append("<p>DOCX 在线预览</p>");
            html.append("</header>");

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                appendParagraph(html, paragraph);
            }
            for (XWPFTable table : document.getTables()) {
                appendTable(html, table);
            }

            if (document.getParagraphs().isEmpty() && document.getTables().isEmpty()) {
                html.append("<p class=\"docx-preview-empty\">文档内容为空</p>");
            }
            html.append("</section>");
            return html.toString();
        } catch (IOException ex) {
            log.warn("DOCX 预览解析失败: file={}, message={}", fileName, ex.getMessage());
            throw ex;
        }
    }

    private void appendParagraph(StringBuilder html, XWPFParagraph paragraph) {
        String text = paragraph.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        String style = paragraph.getStyle();
        String escaped = HtmlUtils.htmlEscape(text);
        if (style != null && style.toLowerCase(Locale.ROOT).startsWith("heading")) {
            html.append("<h2>").append(escaped).append("</h2>");
            return;
        }
        html.append("<p>").append(escaped).append("</p>");
    }

    private void appendTable(StringBuilder html, XWPFTable table) {
        html.append("<table class=\"docx-preview-table\"><tbody>");
        table.getRows().forEach(row -> {
            html.append("<tr>");
            row.getTableCells().forEach(cell -> html.append("<td>")
                    .append(HtmlUtils.htmlEscape(cell.getText()))
                    .append("</td>"));
            html.append("</tr>");
        });
        html.append("</tbody></table>");
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
