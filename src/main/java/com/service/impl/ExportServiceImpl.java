package com.service.impl;

import com.exception.BusinessException;
import com.service.ExportService;
import com.service.GradeService;
import com.service.InternshipProjectService;
import com.vo.GradeVO;
import com.vo.ProjectVO;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final float PAGE_MARGIN = 42F;
    private static final float TABLE_HEADER_HEIGHT = 28F;
    private static final float TABLE_ROW_HEIGHT = 24F;
    private static final float FOOTER_SPACE = 32F;
    private static final Color ACCENT_COLOR = new Color(29, 78, 216);
    private static final Color ACCENT_SOFT = new Color(219, 234, 254);
    private static final Color HEADER_BG = new Color(235, 243, 255);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color TEXT_COLOR = new Color(17, 24, 39);
    private static final Color MUTED_TEXT = new Color(100, 116, 139);
    private static final Color ALT_ROW_BG = new Color(248, 250, 252);
    private static final String SYSTEM_FOOTER = "学生实习与实践教学管理系统导出报表";

    private final GradeService gradeService;
    private final InternshipProjectService projectService;

    @Override
    public byte[] exportGradesExcel(String semester, String creditType) {
        List<GradeVO> grades = gradeService.exportable(semester, creditType);
        List<ExcelColumn> columns = List.of(
                new ExcelColumn("学生姓名", 12, TextAlign.LEFT, false),
                new ExcelColumn("项目名称", 30, TextAlign.LEFT, false),
                new ExcelColumn("指导教师", 12, TextAlign.LEFT, false),
                new ExcelColumn("日志成绩", 10, TextAlign.CENTER, true),
                new ExcelColumn("报告成绩", 10, TextAlign.CENTER, true),
                new ExcelColumn("综合成绩", 10, TextAlign.CENTER, true),
                new ExcelColumn("终审状态", 12, TextAlign.CENTER, false)
        );
        List<List<Object>> rows = new ArrayList<>();
        for (GradeVO item : grades) {
            rows.add(List.of(
                    defaultValue(item.getStudentName()),
                    defaultValue(item.getProjectTitle()),
                    defaultValue(item.getTeacherName()),
                    item.getJournalScore() == null ? 0D : item.getJournalScore().doubleValue(),
                    item.getReportScore() == null ? 0D : item.getReportScore().doubleValue(),
                    item.getFinalScore() == null ? 0D : item.getFinalScore().doubleValue(),
                    translateAdminStatus(item.getAdminStatus())
            ));
        }
        List<String> metaLines = List.of(
                "学期筛选：" + filterValue(semester) + "    学分类型：" + filterValue(creditType),
                "记录数量：" + grades.size() + "    导出时间：" + LocalDateTime.now().format(EXPORT_TIME_FORMATTER)
        );
        return renderStyledExcel(new ExcelSheetSpec(
                "成绩汇总",
                "学生实习成绩汇总表",
                "用于管理员终审、归档与成绩导出",
                metaLines,
                columns,
                rows
        ), "导出成绩 Excel 失败");
    }

    @Override
    public byte[] exportGradesPdf(String semester, String creditType) {
        List<GradeVO> grades = gradeService.exportable(semester, creditType);
        List<PdfColumn> columns = List.of(
                new PdfColumn("学生", 82F, TextAlign.LEFT),
                new PdfColumn("项目", 250F, TextAlign.LEFT),
                new PdfColumn("指导教师", 90F, TextAlign.LEFT),
                new PdfColumn("日志", 56F, TextAlign.CENTER),
                new PdfColumn("报告", 56F, TextAlign.CENTER),
                new PdfColumn("总评", 56F, TextAlign.CENTER),
                new PdfColumn("状态", 78F, TextAlign.CENTER)
        );
        List<List<String>> rows = new ArrayList<>();
        for (GradeVO item : grades) {
            rows.add(List.of(
                    defaultValue(item.getStudentName()),
                    defaultValue(item.getProjectTitle()),
                    defaultValue(item.getTeacherName()),
                    scoreText(item.getJournalScore()),
                    scoreText(item.getReportScore()),
                    scoreText(item.getFinalScore()),
                    translateAdminStatus(item.getAdminStatus())
            ));
        }
        if (rows.isEmpty()) {
            rows.add(List.of("暂无符合条件的成绩记录", "", "", "", "", "", ""));
        }
        List<String> metaLines = List.of(
                "学期筛选: " + filterValue(semester),
                "学分类型: " + filterValue(creditType),
                "记录数量: " + grades.size(),
                "导出时间: " + LocalDateTime.now().format(EXPORT_TIME_FORMATTER)
        );
        PdfTableSpec spec = new PdfTableSpec(
                "实践成绩汇总表",
                "用于管理员终审、归档与成绩导出",
                metaLines,
                columns,
                rows,
                true
        );
        return renderTablePdf(spec);
    }

    @Override
    public byte[] exportProjectParticipantsExcel(Long projectId) {
        List<UserVO> participants = getParticipantsOrThrow(projectId);
        ProjectVO project = projectService.getDetail(projectId);
        List<ExcelColumn> columns = List.of(
                new ExcelColumn("姓名", 12, TextAlign.LEFT, false),
                new ExcelColumn("登录账号", 18, TextAlign.LEFT, false),
                new ExcelColumn("学号", 16, TextAlign.LEFT, false),
                new ExcelColumn("学院", 16, TextAlign.LEFT, false),
                new ExcelColumn("专业", 18, TextAlign.LEFT, false)
        );
        List<List<Object>> rows = new ArrayList<>();
        for (UserVO item : participants) {
            rows.add(List.of(
                    defaultValue(item.getRealName()),
                    defaultValue(item.getUsername()),
                    defaultValue(item.getStudentNo()),
                    defaultValue(item.getCollege()),
                    defaultValue(item.getMajor())
            ));
        }
        List<String> metaLines = List.of(
                "项目名称：" + defaultValue(project.getTitle()),
                "指导教师：" + defaultValue(project.getTeacherName()) + "    实习单位：" + defaultValue(project.getCompany()),
                "学期：" + defaultValue(project.getSemester()) + "    已通过人数：" + participants.size()
                        + "    导出时间：" + LocalDateTime.now().format(EXPORT_TIME_FORMATTER)
        );
        return renderStyledExcel(new ExcelSheetSpec(
                "参与名单",
                "项目参与学生名单",
                "用于项目归档、教学留存与线下核验",
                metaLines,
                columns,
                rows
        ), "导出参与名单 Excel 失败");
    }

    @Override
    public byte[] exportProjectParticipantsPdf(Long projectId) {
        List<UserVO> participants = getParticipantsOrThrow(projectId);
        ProjectVO project = projectService.getDetail(projectId);
        List<PdfColumn> columns = List.of(
                new PdfColumn("姓名", 74F, TextAlign.LEFT),
                new PdfColumn("账号", 92F, TextAlign.LEFT),
                new PdfColumn("学号", 98F, TextAlign.LEFT),
                new PdfColumn("学院", 118F, TextAlign.LEFT),
                new PdfColumn("专业", 118F, TextAlign.LEFT)
        );
        List<List<String>> rows = new ArrayList<>();
        for (UserVO item : participants) {
            rows.add(List.of(
                    defaultValue(item.getRealName()),
                    defaultValue(item.getUsername()),
                    defaultValue(item.getStudentNo()),
                    defaultValue(item.getCollege()),
                    defaultValue(item.getMajor())
            ));
        }
        List<String> metaLines = List.of(
                "项目名称: " + defaultValue(project.getTitle()),
                "指导教师: " + defaultValue(project.getTeacherName()),
                "实习单位: " + defaultValue(project.getCompany()),
                "学期: " + defaultValue(project.getSemester()) + "    已通过人数: " + participants.size(),
                "导出时间: " + LocalDateTime.now().format(EXPORT_TIME_FORMATTER)
        );
        PdfTableSpec spec = new PdfTableSpec(
                "项目参与名单",
                "用于项目归档、教学留存与线下核验",
                metaLines,
                columns,
                rows,
                false
        );
        return renderTablePdf(spec);
    }

    private List<UserVO> getParticipantsOrThrow(Long projectId) {
        List<UserVO> participants = projectService.listParticipants(projectId, 1, Integer.MAX_VALUE).getRecords();
        if (participants == null || participants.isEmpty()) {
            throw new BusinessException(400, "当前项目暂无已通过学生，无法导出名单");
        }
        return participants;
    }

    // 统一渲染带标题区的 Excel，避免成绩单和参与名单各自维护一套样式逻辑。
    private byte[] renderStyledExcel(ExcelSheetSpec spec, String errorMessage) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(spec.sheetName);
            ExcelStyles styles = createExcelStyles(workbook);
            int lastColumnIndex = spec.columns.size() - 1;
            int rowIndex = 0;

            sheet.setDisplayGridlines(false);
            // 标题、副标题和元信息都写成合并行，让导出文件打开后更像正式报表。
            rowIndex = writeMergedTextRow(sheet, rowIndex, lastColumnIndex, spec.title, styles.title, 28F);
            rowIndex = writeMergedTextRow(sheet, rowIndex, lastColumnIndex, spec.subtitle, styles.subtitle, 20F);
            for (String metaLine : spec.metaLines) {
                rowIndex = writeMergedTextRow(sheet, rowIndex, lastColumnIndex, metaLine, styles.meta, 18F);
            }

            Row spacer = sheet.createRow(rowIndex++);
            spacer.setHeightInPoints(8F);

            int headerRowIndex = rowIndex;
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.setHeightInPoints(24F);
            for (int i = 0; i < spec.columns.size(); i++) {
                ExcelColumn column = spec.columns.get(i);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(column.title);
                cell.setCellStyle(styles.header);
                sheet.setColumnWidth(i, column.width * 256);
            }

            for (int rowNumber = 0; rowNumber < spec.rows.size(); rowNumber++) {
                List<Object> rowValues = spec.rows.get(rowNumber);
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(22F);
                boolean striped = rowNumber % 2 == 1;
                for (int columnIndex = 0; columnIndex < spec.columns.size(); columnIndex++) {
                    ExcelColumn column = spec.columns.get(columnIndex);
                    Cell cell = row.createCell(columnIndex);
                    Object value = columnIndex < rowValues.size() ? rowValues.get(columnIndex) : "";
                    writeExcelCell(cell, value, column, styles, striped);
                }
            }

            sheet.createFreezePane(0, headerRowIndex + 1);
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, headerRowIndex, 0, lastColumnIndex));
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(500, errorMessage);
        }
    }

    // 合并整行单元格用于标题区，只有首列写值，其余单元格保留样式以维持边界一致。
    private int writeMergedTextRow(XSSFSheet sheet, int rowIndex, int lastColumnIndex, String value, CellStyle style, float height) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(height);
        for (int columnIndex = 0; columnIndex <= lastColumnIndex; columnIndex++) {
            Cell cell = row.createCell(columnIndex);
            cell.setCellStyle(style);
            if (columnIndex == 0) {
                cell.setCellValue(defaultValue(value));
            }
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, lastColumnIndex));
        return rowIndex + 1;
    }

    private void writeExcelCell(Cell cell, Object value, ExcelColumn column, ExcelStyles styles, boolean striped) {
        cell.setCellStyle(resolveBodyStyle(styles, column, striped));
        if (column.numeric && value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        cell.setCellValue(value == null ? "-" : String.valueOf(value));
    }

    private CellStyle resolveBodyStyle(ExcelStyles styles, ExcelColumn column, boolean striped) {
        if (column.numeric) {
            return striped ? styles.numberAlt : styles.number;
        }
        if (column.align == TextAlign.CENTER) {
            return striped ? styles.centerAlt : styles.center;
        }
        if (column.align == TextAlign.RIGHT) {
            return striped ? styles.rightAlt : styles.right;
        }
        return striped ? styles.leftAlt : styles.left;
    }

    // POI 的样式对象是 workbook 级别的，集中预创建可避免在大表里反复 new 样式导致性能和数量问题。
    private ExcelStyles createExcelStyles(XSSFWorkbook workbook) {
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setFontName("Microsoft YaHei");

        Font subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 10);
        subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        subtitleFont.setFontName("Microsoft YaHei");

        Font metaFont = workbook.createFont();
        metaFont.setFontHeightInPoints((short) 10);
        metaFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        metaFont.setFontName("Microsoft YaHei");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontName("Microsoft YaHei");

        Font bodyFont = workbook.createFont();
        bodyFont.setFontHeightInPoints((short) 10);
        bodyFont.setFontName("Microsoft YaHei");

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);

        CellStyle subtitleStyle = workbook.createCellStyle();
        subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
        subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        subtitleStyle.setFont(subtitleFont);

        CellStyle metaStyle = workbook.createCellStyle();
        metaStyle.setAlignment(HorizontalAlignment.LEFT);
        metaStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        metaStyle.setFont(metaFont);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        applyThinBorder(headerStyle);

        CellStyle leftStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.LEFT, false, false);
        CellStyle leftAltStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.LEFT, true, false);
        CellStyle centerStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.CENTER, false, false);
        CellStyle centerAltStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.CENTER, true, false);
        CellStyle rightStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.RIGHT, false, false);
        CellStyle rightAltStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.RIGHT, true, false);
        CellStyle numberStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.CENTER, false, true);
        CellStyle numberAltStyle = createBodyStyle(workbook, bodyFont, HorizontalAlignment.CENTER, true, true);

        return new ExcelStyles(
                titleStyle,
                subtitleStyle,
                metaStyle,
                headerStyle,
                leftStyle,
                leftAltStyle,
                centerStyle,
                centerAltStyle,
                rightStyle,
                rightAltStyle,
                numberStyle,
                numberAltStyle
        );
    }

    private CellStyle createBodyStyle(XSSFWorkbook workbook, Font font, HorizontalAlignment alignment, boolean striped, boolean numeric) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font);
        if (striped) {
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (numeric) {
            style.setDataFormat(workbook.createDataFormat().getFormat("0.##"));
        }
        applyThinBorder(style);
        return style;
    }

    private void applyThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private byte[] renderTablePdf(PdfTableSpec spec) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfFonts fonts = loadFonts(document);
            PdfPageContext pageContext = startPage(document, spec, fonts, 1, true);
            drawTableHeader(pageContext, spec, fonts);
            for (int rowIndex = 0; rowIndex < spec.rows.size(); rowIndex++) {
                if (pageContext.cursorY - TABLE_ROW_HEIGHT < PAGE_MARGIN + FOOTER_SPACE) {
                    finishPage(pageContext, fonts);
                    pageContext = startPage(document, spec, fonts, pageContext.pageNumber + 1, false);
                    drawTableHeader(pageContext, spec, fonts);
                }
                drawTableRow(pageContext, spec, fonts, spec.rows.get(rowIndex), rowIndex);
            }
            finishPage(pageContext, fonts);
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(500, "导出 PDF 失败");
        }
    }

    private PdfPageContext startPage(PDDocument document, PdfTableSpec spec, PdfFonts fonts, int pageNumber, boolean firstPage)
            throws IOException {
        PDRectangle pageSize = spec.landscape
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;
        PDPage page = new PDPage(pageSize);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        float pageWidth = pageSize.getWidth();
        float pageHeight = pageSize.getHeight();
        float cursorY = pageHeight - PAGE_MARGIN;

        stream.setNonStrokingColor(ACCENT_COLOR);
        stream.addRect(PAGE_MARGIN, cursorY - 30F, 6F, 32F);
        stream.fill();
        drawText(stream, fonts.bold, 20F, spec.title, PAGE_MARGIN + 16F, cursorY - 8F, TEXT_COLOR);
        drawText(stream, fonts.regular, 10F, spec.subtitle, PAGE_MARGIN + 16F, cursorY - 24F, MUTED_TEXT);
        drawText(stream, fonts.regular, 10F, "第 " + pageNumber + " 页", pageWidth - PAGE_MARGIN - 44F, cursorY - 8F, MUTED_TEXT);
        cursorY -= 46F;

        if (firstPage && !spec.metaLines.isEmpty()) {
            float boxHeight = 18F + spec.metaLines.size() * 15F;
            stream.setNonStrokingColor(ACCENT_SOFT);
            stream.addRect(PAGE_MARGIN, cursorY - boxHeight, pageWidth - (PAGE_MARGIN * 2), boxHeight);
            stream.fill();
            stream.setStrokingColor(BORDER_COLOR);
            stream.addRect(PAGE_MARGIN, cursorY - boxHeight, pageWidth - (PAGE_MARGIN * 2), boxHeight);
            stream.stroke();
            float textY = cursorY - 16F;
            for (String line : spec.metaLines) {
                drawText(stream, fonts.regular, 10F, fitText(line, fonts.regular, 10F, pageWidth - (PAGE_MARGIN * 2) - 18F),
                        PAGE_MARGIN + 10F, textY, MUTED_TEXT);
                textY -= 15F;
            }
            cursorY -= boxHeight + 16F;
        } else {
            cursorY -= 12F;
        }

        return new PdfPageContext(page, stream, pageWidth, pageHeight, cursorY, pageNumber);
    }

    private void drawTableHeader(PdfPageContext pageContext, PdfTableSpec spec, PdfFonts fonts) throws IOException {
        float x = PAGE_MARGIN;
        float y = pageContext.cursorY;
        for (PdfColumn column : spec.columns) {
            pageContext.stream.setNonStrokingColor(HEADER_BG);
            pageContext.stream.addRect(x, y - TABLE_HEADER_HEIGHT, column.width, TABLE_HEADER_HEIGHT);
            pageContext.stream.fill();
            pageContext.stream.setStrokingColor(BORDER_COLOR);
            pageContext.stream.addRect(x, y - TABLE_HEADER_HEIGHT, column.width, TABLE_HEADER_HEIGHT);
            pageContext.stream.stroke();
            drawCellText(pageContext.stream, fonts.bold, 10F, column.title, x, y, column.width, TABLE_HEADER_HEIGHT, TextAlign.CENTER, TEXT_COLOR);
            x += column.width;
        }
        pageContext.cursorY -= TABLE_HEADER_HEIGHT;
    }

    private void drawTableRow(PdfPageContext pageContext, PdfTableSpec spec, PdfFonts fonts, List<String> row, int rowIndex) throws IOException {
        float x = PAGE_MARGIN;
        float y = pageContext.cursorY;
        for (int columnIndex = 0; columnIndex < spec.columns.size(); columnIndex++) {
            PdfColumn column = spec.columns.get(columnIndex);
            if (rowIndex % 2 == 1) {
                pageContext.stream.setNonStrokingColor(ALT_ROW_BG);
                pageContext.stream.addRect(x, y - TABLE_ROW_HEIGHT, column.width, TABLE_ROW_HEIGHT);
                pageContext.stream.fill();
            }
            pageContext.stream.setStrokingColor(BORDER_COLOR);
            pageContext.stream.addRect(x, y - TABLE_ROW_HEIGHT, column.width, TABLE_ROW_HEIGHT);
            pageContext.stream.stroke();
            String cellValue = columnIndex < row.size() ? row.get(columnIndex) : "";
            drawCellText(pageContext.stream, fonts.regular, 9.5F, cellValue, x, y, column.width, TABLE_ROW_HEIGHT, column.align, TEXT_COLOR);
            x += column.width;
        }
        pageContext.cursorY -= TABLE_ROW_HEIGHT;
    }

    private void drawCellText(PDPageContentStream stream, PDFont font, float fontSize, String text, float x, float y,
                              float width, float height, TextAlign align, Color color) throws IOException {
        String safeText = fitText(defaultValue(text), font, fontSize, width - 10F);
        float textWidth = textWidth(font, fontSize, safeText);
        float textX = x + 6F;
        if (align == TextAlign.CENTER) {
            textX = x + (width - textWidth) / 2F;
        } else if (align == TextAlign.RIGHT) {
            textX = x + width - textWidth - 6F;
        }
        float textY = y - ((height - fontSize) / 2F) - 4F;
        drawText(stream, font, fontSize, safeText, textX, textY, color);
    }

    private void drawText(PDPageContentStream stream, PDFont font, float fontSize, String text, float x, float y, Color color)
            throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(text == null ? "" : text);
        stream.endText();
    }

    private void finishPage(PdfPageContext pageContext, PdfFonts fonts) throws IOException {
        float footerY = PAGE_MARGIN - 10F;
        pageContext.stream.setStrokingColor(BORDER_COLOR);
        pageContext.stream.moveTo(PAGE_MARGIN, footerY + 12F);
        pageContext.stream.lineTo(pageContext.pageWidth - PAGE_MARGIN, footerY + 12F);
        pageContext.stream.stroke();
        drawText(pageContext.stream, fonts.regular, 9F, SYSTEM_FOOTER, PAGE_MARGIN, footerY, MUTED_TEXT);
        drawText(pageContext.stream, fonts.regular, 9F, "第 " + pageContext.pageNumber + " 页",
                pageContext.pageWidth - PAGE_MARGIN - 36F, footerY, MUTED_TEXT);
        pageContext.stream.close();
    }

    private PdfFonts loadFonts(PDDocument document) throws IOException {
        PDFont regular = loadFont(document, new String[]{
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/simhei.ttf"
        });
        PDFont bold = loadFont(document, new String[]{
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsunb.ttf",
                "C:/Windows/Fonts/msyhbd.ttc",
                "C:/Windows/Fonts/msyh.ttc"
        });
        return new PdfFonts(regular, bold);
    }

    private PDFont loadFont(PDDocument document, String[] paths) throws IOException {
        for (String path : paths) {
            File fontFile = new File(path);
            if (!fontFile.exists()) {
                continue;
            }
            try {
                return PDType0Font.load(document, fontFile);
            } catch (IOException ex) {
                log.warn("PDF 字体加载失败，尝试下一个字体: path={}, message={}", path, ex.getMessage());
            }
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private String fitText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        String safeText = text == null ? "" : text;
        if (safeText.isBlank()) {
            return "-";
        }
        if (textWidth(font, fontSize, safeText) <= maxWidth) {
            return safeText;
        }
        String ellipsis = "...";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < safeText.length(); i++) {
            String candidate = builder + safeText.substring(i, i + 1);
            if (textWidth(font, fontSize, candidate + ellipsis) > maxWidth) {
                break;
            }
            builder.append(safeText, i, i + 1);
        }
        return builder + ellipsis;
    }

    private float textWidth(PDFont font, float fontSize, String text) throws IOException {
        return font.getStringWidth(text == null ? "" : text) / 1000F * fontSize;
    }

    private String translateAdminStatus(String status) {
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            return "已确认";
        }
        if ("RETURNED".equalsIgnoreCase(status)) {
            return "已退回";
        }
        if ("PENDING".equalsIgnoreCase(status)) {
            return "待终审";
        }
        return defaultValue(status);
    }

    private String scoreText(Object score) {
        return score == null ? "-" : score.toString();
    }

    private String filterValue(String value) {
        return value == null || value.isBlank() ? "全部" : value;
    }

    private String defaultValue(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private record PdfColumn(String title, float width, TextAlign align) {
    }

    private record ExcelColumn(String title, int width, TextAlign align, boolean numeric) {
    }

    private record PdfFonts(PDFont regular, PDFont bold) {
    }

    private record ExcelSheetSpec(String sheetName, String title, String subtitle, List<String> metaLines,
                                  List<ExcelColumn> columns, List<List<Object>> rows) {
    }

    private record ExcelStyles(CellStyle title, CellStyle subtitle, CellStyle meta, CellStyle header,
                               CellStyle left, CellStyle leftAlt, CellStyle center, CellStyle centerAlt,
                               CellStyle right, CellStyle rightAlt, CellStyle number, CellStyle numberAlt) {
    }

    private enum TextAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    private static class PdfTableSpec {
        private final String title;
        private final String subtitle;
        private final List<String> metaLines;
        private final List<PdfColumn> columns;
        private final List<List<String>> rows;
        private final boolean landscape;

        private PdfTableSpec(String title, String subtitle, List<String> metaLines, List<PdfColumn> columns,
                             List<List<String>> rows, boolean landscape) {
            this.title = title;
            this.subtitle = subtitle;
            this.metaLines = metaLines;
            this.columns = columns;
            this.rows = rows;
            this.landscape = landscape;
        }
    }

    private static class PdfPageContext {
        private final PDPage page;
        private final PDPageContentStream stream;
        private final float pageWidth;
        private final float pageHeight;
        private float cursorY;
        private final int pageNumber;

        private PdfPageContext(PDPage page, PDPageContentStream stream, float pageWidth, float pageHeight,
                               float cursorY, int pageNumber) {
            this.page = page;
            this.stream = stream;
            this.pageWidth = pageWidth;
            this.pageHeight = pageHeight;
            this.cursorY = cursorY;
            this.pageNumber = pageNumber;
        }
    }
}
