package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.PageResult;
import com.common.PageUtils;
import com.common.enums.AdminApprovalStatus;
import com.common.enums.ApprovalStatus;
import com.dto.GradeConfirmDTO;
import com.dto.GradeDTO;
import com.entity.Grade;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.exception.BusinessException;
import com.mapper.GradeMapper;
import com.security.UserContext;
import com.service.ApplicationService;
import com.service.GradeService;
import com.service.InternshipProjectService;
import com.service.support.ViewAssembler;
import com.vo.GradeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeMapper gradeMapper;
    private final ApplicationService applicationService;
    private final InternshipProjectService projectService;
    private final ViewAssembler viewAssembler;

    @Override
    public GradeVO create(GradeDTO dto) {
        InternApplication application = applicationService.getByIdOrThrow(dto.getApplicationId());
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        if (!ApprovalStatus.APPROVED.name().equals(application.getStatus())) {
            throw new BusinessException(400, "仅已通过申请可评分");
        }
        Long count = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getApplicationId, dto.getApplicationId()));
        if (count != null && count > 0) {
            throw new BusinessException(400, "当前申请已存在成绩记录");
        }
        Grade grade = new Grade();
        fillGrade(grade, dto, application, project);
        gradeMapper.insert(grade);
        log.info("成绩已创建: gradeId={}, applicationId={}, studentId={}, teacherId={}, finalScore={}, adminStatus={}",
                grade.getId(), grade.getApplicationId(), grade.getStudentId(), grade.getTeacherId(),
                grade.getFinalScore(), grade.getAdminStatus());
        return viewAssembler.toGradeVO(grade);
    }

    @Override
    public GradeVO update(Long id, GradeDTO dto) {
        Grade grade = getByIdOrThrow(id);
        InternApplication application = applicationService.getByIdOrThrow(grade.getApplicationId());
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        fillGrade(grade, dto, application, project);
        gradeMapper.updateById(grade);
        log.info("成绩已更新: gradeId={}, applicationId={}, studentId={}, teacherId={}, finalScore={}, adminStatus={}",
                grade.getId(), grade.getApplicationId(), grade.getStudentId(), grade.getTeacherId(),
                grade.getFinalScore(), grade.getAdminStatus());
        return viewAssembler.toGradeVO(grade);
    }

    @Override
    public GradeVO getDetail(Long id) {
        Grade grade = getByIdOrThrow(id);
        authorizeRead(grade);
        return viewAssembler.toGradeVO(grade);
    }

    @Override
    public PageResult<GradeVO> listMy(int page, int size) {
        List<GradeVO> records = gradeMapper.selectList(new LambdaQueryWrapper<Grade>()
                        .eq(Grade::getStudentId, UserContext.getCurrentUserId())
                        .orderByDesc(Grade::getCreateTime))
                .stream()
                .map(viewAssembler::toGradeVO)
                .toList();
        return PageUtils.fromList(records, page, size);
    }

    @Override
    public PageResult<GradeVO> listTeacher(int page, int size) {
        List<GradeVO> records = gradeMapper.selectList(new LambdaQueryWrapper<Grade>()
                        .eq(Grade::getTeacherId, UserContext.getCurrentUserId())
                        .orderByDesc(Grade::getCreateTime))
                .stream()
                .map(viewAssembler::toGradeVO)
                .toList();
        return PageUtils.fromList(records, page, size);
    }

    @Override
    public PageResult<GradeVO> listAdmin(int page, int size, String status, String semester, String creditType) {
        List<GradeVO> records = gradeMapper.selectList(new LambdaQueryWrapper<Grade>()
                        .orderByDesc(Grade::getCreateTime))
                .stream()
                .map(viewAssembler::toGradeVO)
                .filter(item -> status == null || status.isBlank() || status.equals(item.getAdminStatus()))
                .filter(item -> matchProjectFilter(item.getApplicationId(), semester, creditType))
                .toList();
        return PageUtils.fromList(records, page, size);
    }

    @Override
    public GradeVO confirm(Long id, GradeConfirmDTO dto) {
        Grade grade = getByIdOrThrow(id);
        grade.setAdminStatus(dto.getAdminStatus().name());
        grade.setAdminComment(dto.getAdminComment());
        grade.setConfirmedBy(UserContext.getCurrentUserId());
        grade.setConfirmedAt(LocalDateTime.now());
        gradeMapper.updateById(grade);
        log.info("成绩终审完成: gradeId={}, adminId={}, adminStatus={}, finalScore={}",
                grade.getId(), UserContext.getCurrentUserId(), grade.getAdminStatus(), grade.getFinalScore());
        return viewAssembler.toGradeVO(grade);
    }

    @Override
    public List<GradeVO> exportable(String semester, String creditType) {
        return listAdmin(1, Integer.MAX_VALUE, null, semester, creditType).getRecords();
    }

    private Grade getByIdOrThrow(Long id) {
        Grade grade = gradeMapper.selectById(id);
        if (grade == null || (grade.getDeleted() != null && grade.getDeleted() == 1)) {
            throw new BusinessException(404, "成绩不存在");
        }
        return grade;
    }

    private void fillGrade(Grade grade, GradeDTO dto, InternApplication application, InternshipProject project) {
        grade.setApplicationId(application.getId());
        grade.setStudentId(application.getStudentId());
        grade.setTeacherId(project.getTeacherId());
        grade.setJournalScore(dto.getJournalScore());
        grade.setReportScore(dto.getReportScore());
        grade.setFinalScore(calculateFinalScore(dto.getJournalScore(), dto.getReportScore()));
        grade.setTeacherComment(dto.getTeacherComment());
        if (grade.getAdminStatus() == null) {
            grade.setAdminStatus(AdminApprovalStatus.PENDING.name());
        }
    }

    private BigDecimal calculateFinalScore(BigDecimal journalScore, BigDecimal reportScore) {
        return journalScore.multiply(BigDecimal.valueOf(0.4))
                .add(reportScore.multiply(BigDecimal.valueOf(0.6)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void assertTeacherOwner(InternshipProject project) {
        if (!project.getTeacherId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "仅指导教师可操作");
        }
    }

    private void authorizeRead(Grade grade) {
        if (grade.getStudentId().equals(UserContext.getCurrentUserId())
                || grade.getTeacherId().equals(UserContext.getCurrentUserId())
                || "ADMIN".equals(UserContext.getCurrentRole())) {
            return;
        }
        throw new BusinessException(403, "无权限查看成绩");
    }

    private boolean matchProjectFilter(Long applicationId, String semester, String creditType) {
        InternApplication application = applicationService.getByIdOrThrow(applicationId);
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        boolean semesterMatched = semester == null || semester.isBlank() || semester.equals(project.getSemester());
        boolean creditMatched = creditType == null || creditType.isBlank() || creditType.equals(project.getCreditType());
        return semesterMatched && creditMatched;
    }
}
