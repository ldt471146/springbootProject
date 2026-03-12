package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.PageResult;
import com.common.PageUtils;
import com.common.enums.ApprovalStatus;
import com.common.enums.ReportStatus;
import com.dto.ReportDTO;
import com.dto.ReportReviewDTO;
import com.entity.Attachment;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.entity.Report;
import com.exception.BusinessException;
import com.mapper.AttachmentMapper;
import com.mapper.ReportMapper;
import com.security.UserContext;
import com.service.ApplicationService;
import com.service.FileStorageService;
import com.service.InternshipProjectService;
import com.service.ReportService;
import com.service.support.ViewAssembler;
import com.vo.AttachmentVO;
import com.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String REPORT_ATTACHMENT_REF_TYPE = "REPORT_ATTACHMENT";

    private final AttachmentMapper attachmentMapper;
    private final ReportMapper reportMapper;
    private final ApplicationService applicationService;
    private final InternshipProjectService projectService;
    private final FileStorageService fileStorageService;
    private final ViewAssembler viewAssembler;

    @Override
    public ReportVO create(ReportDTO dto) {
        InternApplication application = applicationService.getByIdOrThrow(dto.getApplicationId());
        assertStudentOwner(application);
        if (!ApprovalStatus.APPROVED.name().equals(application.getStatus())) {
            throw new BusinessException(400, "仅已通过申请可提交报告");
        }
        Long count = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getApplicationId, dto.getApplicationId())
                .eq(Report::getStudentId, UserContext.getCurrentUserId()));
        if (count != null && count > 0) {
            throw new BusinessException(400, "当前申请已提交报告，请使用重新提交");
        }
        Report report = new Report();
        fillReport(report, dto);
        report.setStudentId(UserContext.getCurrentUserId());
        report.setStatus(ReportStatus.SUBMITTED.name());
        report.setIsExcellent(0);
        report.setAiFlag(analyzeAiFlag(dto.getSummary()));
        reportMapper.insert(report);
        log.info("报告已提交: reportId={}, applicationId={}, studentId={}, aiFlag={}",
                report.getId(), report.getApplicationId(), report.getStudentId(), report.getAiFlag());
        return viewAssembler.toReportVO(report);
    }

    @Override
    public ReportVO update(Long id, ReportDTO dto) {
        Report report = getByIdOrThrow(id);
        assertStudentReportOwner(report);
        fillReport(report, dto);
        report.setStatus(ReportStatus.SUBMITTED.name());
        report.setTeacherComment(null);
        report.setTeacherScore(null);
        report.setIsExcellent(0);
        report.setReviewedBy(null);
        report.setReviewedAt(null);
        report.setAiFlag(analyzeAiFlag(dto.getSummary()));
        reportMapper.updateById(report);
        log.info("报告已更新并重新提交: reportId={}, applicationId={}, studentId={}, aiFlag={}",
                report.getId(), report.getApplicationId(), report.getStudentId(), report.getAiFlag());
        return viewAssembler.toReportVO(report);
    }

    @Override
    public ReportVO getDetail(Long id) {
        Report report = getByIdOrThrow(id);
        authorizeRead(report);
        return viewAssembler.toReportVO(report);
    }

    @Override
    public PageResult<ReportVO> listMy(int page, int size) {
        List<ReportVO> records = reportMapper.selectList(new LambdaQueryWrapper<Report>()
                        .eq(Report::getStudentId, UserContext.getCurrentUserId())
                        .orderByDesc(Report::getCreateTime))
                .stream()
                .map(viewAssembler::toReportVO)
                .toList();
        return PageUtils.fromList(records, page, size);
    }

    @Override
    public PageResult<ReportVO> teacherList(int page, int size, String status) {
        Set<Long> projectIds = getCurrentTeacherProjectIds();
        if (projectIds.isEmpty()) {
            return PageResult.empty(page, size);
        }
        List<ReportVO> records = reportMapper.selectList(new LambdaQueryWrapper<Report>()
                        .orderByDesc(Report::getCreateTime))
                .stream()
                .filter(report -> {
                    InternApplication application = applicationService.getByIdOrThrow(report.getApplicationId());
                    return projectIds.contains(application.getProjectId());
                })
                .filter(report -> status == null || status.isBlank() || status.equals(report.getStatus()))
                .map(viewAssembler::toReportVO)
                .toList();
        return PageUtils.fromList(records, page, size);
    }

    @Override
    public ReportVO review(Long id, ReportReviewDTO dto) {
        Report report = getByIdOrThrow(id);
        InternApplication application = applicationService.getByIdOrThrow(report.getApplicationId());
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        report.setTeacherComment(dto.getComment());
        report.setTeacherScore(dto.getScore());
        report.setReviewedBy(UserContext.getCurrentUserId());
        report.setReviewedAt(LocalDateTime.now());
        int excellent = Boolean.TRUE.equals(dto.getIsExcellent()) ? 1 : 0;
        report.setIsExcellent(excellent);
        report.setStatus(excellent == 1 ? ReportStatus.EXCELLENT.name() : dto.getStatus().name());
        reportMapper.updateById(report);
        log.info("报告评阅完成: reportId={}, teacherId={}, status={}, score={}, excellent={}",
                report.getId(), UserContext.getCurrentUserId(), report.getStatus(), report.getTeacherScore(), excellent);
        return viewAssembler.toReportVO(report);
    }

    @Override
    public AttachmentVO upload(MultipartFile file) {
        AttachmentVO attachment = fileStorageService.store(file, "REPORT", 0L);
        log.info("报告附件上传: userId={}, originalName={}, fileUrl={}",
                UserContext.getCurrentUserId(), attachment.getOriginalName(), attachment.getFileUrl());
        return attachment;
    }

    @Override
    public AttachmentVO uploadAttachment(Long reportId, MultipartFile file) {
        Report report = getByIdOrThrow(reportId);
        assertStudentReportOwner(report);
        AttachmentVO attachment = fileStorageService.store(file, REPORT_ATTACHMENT_REF_TYPE, reportId);
        log.info("报告附加材料已上传: reportId={}, attachmentId={}, userId={}, originalName={}",
                reportId, attachment.getId(), UserContext.getCurrentUserId(), attachment.getOriginalName());
        return attachment;
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = getAttachmentByIdOrThrow(attachmentId);
        Report report = getByIdOrThrow(attachment.getRefId());
        assertStudentReportOwner(report);
        attachmentMapper.deleteById(attachmentId);
        log.info("报告附加材料已删除: reportId={}, attachmentId={}, userId={}",
                report.getId(), attachmentId, UserContext.getCurrentUserId());
    }

    @Override
    public Attachment getAttachmentByIdOrThrow(Long attachmentId) {
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || (attachment.getDeleted() != null && attachment.getDeleted() == 1)) {
            throw new BusinessException(404, "附件不存在");
        }
        if (!REPORT_ATTACHMENT_REF_TYPE.equals(attachment.getRefType())) {
            throw new BusinessException(400, "附件类型不匹配");
        }
        Report report = getByIdOrThrow(attachment.getRefId());
        authorizeRead(report);
        return attachment;
    }

    @Override
    public Report getByIdOrThrow(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null || (report.getDeleted() != null && report.getDeleted() == 1)) {
            throw new BusinessException(404, "报告不存在");
        }
        return report;
    }

    private void fillReport(Report report, ReportDTO dto) {
        report.setApplicationId(dto.getApplicationId());
        report.setTitle(dto.getTitle());
        report.setSummary(dto.getSummary());
        report.setFileUrl(dto.getFileUrl());
        report.setSemester(dto.getSemester());
        report.setCreditType(dto.getCreditType());
    }

    private String analyzeAiFlag(String summary) {
        if (summary == null || summary.isBlank() || summary.length() < 60) {
            return "TOO_SHORT";
        }
        if (summary.contains("模板") || summary.contains("示例")) {
            return "TEMPLATE_SUSPECTED";
        }
        return "NORMAL";
    }

    private void assertStudentOwner(InternApplication application) {
        if (!application.getStudentId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "只能操作自己的报告");
        }
    }

    private void assertStudentReportOwner(Report report) {
        if (!report.getStudentId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "只能操作自己的报告");
        }
    }

    private void authorizeRead(Report report) {
        if (report.getStudentId().equals(UserContext.getCurrentUserId())) {
            return;
        }
        InternApplication application = applicationService.getByIdOrThrow(report.getApplicationId());
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        if (project.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return;
        }
        if ("ADMIN".equals(UserContext.getCurrentRole())) {
            return;
        }
        throw new BusinessException(403, "无权限查看报告");
    }

    private void assertTeacherOwner(InternshipProject project) {
        if (!project.getTeacherId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "仅指导教师可操作");
        }
    }

    private Set<Long> getCurrentTeacherProjectIds() {
        List<InternshipProject> projects = projectService.listMyProjects(1, Integer.MAX_VALUE).getRecords().stream()
                .map(item -> projectService.getByIdOrThrow(item.getId()))
                .toList();
        if (projects.isEmpty()) {
            return Collections.emptySet();
        }
        return projects.stream().map(InternshipProject::getId).collect(Collectors.toSet());
    }
}
