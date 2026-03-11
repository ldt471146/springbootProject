package com.service.impl;

import com.exception.BusinessException;
import com.service.ExportService;
import com.service.GradeService;
import com.service.InternshipProjectService;
import com.vo.GradeVO;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final GradeService gradeService;
    private final InternshipProjectService projectService;

    @Override
    public byte[] exportGradesExcel(String semester, String creditType) {
        List<GradeVO> grades = gradeService.exportable(semester, creditType);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("grades");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Student");
            header.createCell(1).setCellValue("Project");
            header.createCell(2).setCellValue("Teacher");
            header.createCell(3).setCellValue("Journal");
            header.createCell(4).setCellValue("Report");
            header.createCell(5).setCellValue("Final");
            header.createCell(6).setCellValue("AdminStatus");
            for (int i = 0; i < grades.size(); i++) {
                GradeVO item = grades.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getStudentName());
                row.createCell(1).setCellValue(item.getProjectTitle());
                row.createCell(2).setCellValue(item.getTeacherName());
                row.createCell(3).setCellValue(item.getJournalScore() == null ? 0 : item.getJournalScore().doubleValue());
                row.createCell(4).setCellValue(item.getReportScore() == null ? 0 : item.getReportScore().doubleValue());
                row.createCell(5).setCellValue(item.getFinalScore() == null ? 0 : item.getFinalScore().doubleValue());
                row.createCell(6).setCellValue(item.getAdminStatus());
            }
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(500, "导出成绩 Excel 失败");
        }
    }

    @Override
    public byte[] exportGradesPdf(String semester, String creditType) {
        List<GradeVO> grades = gradeService.exportable(semester, creditType);
        List<String> lines = new ArrayList<>();
        lines.add("Grades Export");
        lines.add("==============================");
        for (GradeVO item : grades) {
            lines.add(String.format("%s | %s | %s | final=%s | status=%s",
                    item.getStudentName(),
                    item.getProjectTitle(),
                    item.getTeacherName(),
                    item.getFinalScore(),
                    item.getAdminStatus()));
        }
        return renderPdf(lines);
    }

    @Override
    public byte[] exportProjectParticipantsExcel(Long projectId) {
        List<UserVO> participants = projectService.listParticipants(projectId, 1, Integer.MAX_VALUE).getRecords();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("participants");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Username");
            header.createCell(2).setCellValue("StudentNo");
            header.createCell(3).setCellValue("College");
            header.createCell(4).setCellValue("Major");
            for (int i = 0; i < participants.size(); i++) {
                UserVO item = participants.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getRealName());
                row.createCell(1).setCellValue(item.getUsername());
                row.createCell(2).setCellValue(item.getStudentNo());
                row.createCell(3).setCellValue(item.getCollege());
                row.createCell(4).setCellValue(item.getMajor());
            }
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(500, "导出参与名单 Excel 失败");
        }
    }

    @Override
    public byte[] exportProjectParticipantsPdf(Long projectId) {
        List<UserVO> participants = projectService.listParticipants(projectId, 1, Integer.MAX_VALUE).getRecords();
        List<String> lines = new ArrayList<>();
        lines.add("Participants Export");
        lines.add("==============================");
        for (UserVO item : participants) {
            lines.add(String.format("%s | %s | %s | %s",
                    item.getRealName(),
                    item.getStudentNo(),
                    item.getCollege(),
                    item.getMajor()));
        }
        return renderPdf(lines);
    }

    private byte[] renderPdf(List<String> lines) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDFont font = loadFont(document);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.setLeading(16);
            contentStream.newLineAtOffset(40, 800);
            int lineCount = 0;
            for (String line : lines) {
                if (lineCount > 45) {
                    contentStream.endText();
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.setLeading(16);
                    contentStream.newLineAtOffset(40, 800);
                    lineCount = 0;
                }
                contentStream.showText(line == null ? "" : line);
                contentStream.newLine();
                lineCount++;
            }
            contentStream.endText();
            contentStream.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(500, "导出 PDF 失败");
        }
    }

    private PDFont loadFont(PDDocument document) throws IOException {
        String[] paths = {
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/simhei.ttf"
        };
        for (String path : paths) {
            File fontFile = new File(path);
            if (fontFile.exists()) {
                return PDType0Font.load(document, fontFile);
            }
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
}
