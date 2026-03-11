package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.PageResult;
import com.common.PageUtils;
import com.common.enums.ApprovalStatus;
import com.common.enums.InternshipPhase;
import com.common.enums.ProjectStatus;
import com.common.enums.RoleEnum;
import com.dto.ApplicationDTO;
import com.dto.ApplicationPhaseDTO;
import com.dto.ApplicationReviewDTO;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.entity.User;
import com.exception.BusinessException;
import com.mapper.ApplicationMapper;
import com.security.UserContext;
import com.service.ApplicationService;
import com.service.InternshipProjectService;
import com.service.UserService;
import com.service.support.ViewAssembler;
import com.vo.ApplicationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final InternshipProjectService projectService;
    private final UserService userService;
    private final ViewAssembler viewAssembler;

    @Override
    public ApplicationVO create(ApplicationDTO dto) {
        InternshipProject project = projectService.getByIdOrThrow(dto.getProjectId());
        User student = userService.getByIdOrThrow(UserContext.getCurrentUserId());
        if (!ProjectStatus.OPEN.name().equals(project.getStatus())) {
            throw new BusinessException(400, "项目未开放报名");
        }
        if (project.getEnrollDeadline() != null && project.getEnrollDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException(400, "项目报名已截止");
        }
        if (project.getCollegeLimit() != null && !project.getCollegeLimit().isBlank()
                && !project.getCollegeLimit().equals(student.getCollege())) {
            throw new BusinessException(400, "当前项目不符合学院限制");
        }
        if (project.getGradeLimit() != null && !project.getGradeLimit().isBlank()
                && !project.getGradeLimit().equals(student.getGrade())) {
            throw new BusinessException(400, "当前项目不符合年级限制");
        }
        Long exists = applicationMapper.selectCount(new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getProjectId, dto.getProjectId())
                .eq(InternApplication::getStudentId, student.getId()));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "你已经申请过该项目");
        }
        if (project.getMaxStudents() != null && project.getMaxStudents() > 0
                && projectService.countApprovedParticipants(project.getId()) >= project.getMaxStudents()) {
            throw new BusinessException(400, "项目名额已满");
        }
        InternApplication application = new InternApplication();
        application.setProjectId(project.getId());
        application.setStudentId(student.getId());
        application.setApplyReason(dto.getApplyReason());
        application.setStatus(ApprovalStatus.PENDING.name());
        application.setPhase(InternshipPhase.ENROLLED.name());
        applicationMapper.insert(application);
        return viewAssembler.toApplicationVO(application);
    }

    @Override
    public PageResult<ApplicationVO> listMyApplications(int page, int size) {
        Page<InternApplication> pager = new Page<>(page, size);
        Page<InternApplication> result = applicationMapper.selectPage(pager, new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getStudentId, UserContext.getCurrentUserId())
                .orderByDesc(InternApplication::getCreateTime));
        List<ApplicationVO> records = result.getRecords().stream().map(viewAssembler::toApplicationVO).toList();
        return PageResult.of(result, records);
    }

    @Override
    public ApplicationVO getDetail(Long id) {
        InternApplication application = getByIdOrThrow(id);
        authorizeView(application);
        return viewAssembler.toApplicationVO(application);
    }

    @Override
    public void withdraw(Long id) {
        InternApplication application = getByIdOrThrow(id);
        if (!application.getStudentId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "只能撤回自己的申请");
        }
        if (!ApprovalStatus.PENDING.name().equals(application.getStatus())) {
            throw new BusinessException(400, "仅待审批申请可撤回");
        }
        applicationMapper.deleteById(id);
    }

    @Override
    public PageResult<ApplicationVO> listProjectApplications(Long projectId, int page, int size, String status) {
        InternshipProject project = projectService.getByIdOrThrow(projectId);
        assertTeacherOwner(project);
        Page<InternApplication> pager = new Page<>(page, size);
        LambdaQueryWrapper<InternApplication> wrapper = new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getProjectId, projectId)
                .orderByDesc(InternApplication::getCreateTime);
        if (status != null && !status.isBlank()) {
            wrapper.eq(InternApplication::getStatus, status);
        }
        Page<InternApplication> result = applicationMapper.selectPage(pager, wrapper);
        List<ApplicationVO> records = result.getRecords().stream().map(viewAssembler::toApplicationVO).toList();
        return PageResult.of(result, records);
    }

    @Override
    public ApplicationVO review(Long id, ApplicationReviewDTO dto) {
        InternApplication application = getByIdOrThrow(id);
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        if (!ApprovalStatus.PENDING.name().equals(application.getStatus())) {
            throw new BusinessException(400, "当前申请已处理");
        }
        if (dto.getStatus() == ApprovalStatus.APPROVED
                && project.getMaxStudents() != null
                && project.getMaxStudents() > 0
                && projectService.countApprovedParticipants(project.getId()) >= project.getMaxStudents()) {
            throw new BusinessException(400, "项目名额已满");
        }
        application.setStatus(dto.getStatus().name());
        application.setReviewComment(dto.getComment());
        application.setReviewedBy(UserContext.getCurrentUserId());
        application.setReviewedAt(LocalDateTime.now());
        applicationMapper.updateById(application);
        return viewAssembler.toApplicationVO(application);
    }

    @Override
    public ApplicationVO updatePhase(Long id, ApplicationPhaseDTO dto) {
        InternApplication application = getByIdOrThrow(id);
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        if (!ApprovalStatus.APPROVED.name().equals(application.getStatus())) {
            throw new BusinessException(400, "仅已通过申请可更新实习阶段");
        }
        application.setPhase(dto.getPhase().name());
        applicationMapper.updateById(application);
        return viewAssembler.toApplicationVO(application);
    }

    @Override
    public PageResult<ApplicationVO> listTeacherStudents(int page, int size, String keyword) {
        Set<Long> projectIds = getCurrentTeacherProjectIds();
        if (projectIds.isEmpty()) {
            return PageResult.empty(page, size);
        }
        List<ApplicationVO> records = applicationMapper.selectList(new LambdaQueryWrapper<InternApplication>()
                        .in(InternApplication::getProjectId, projectIds)
                        .eq(InternApplication::getStatus, ApprovalStatus.APPROVED.name())
                        .orderByDesc(InternApplication::getCreateTime))
                .stream()
                .map(viewAssembler::toApplicationVO)
                .filter(item -> keyword == null || keyword.isBlank()
                        || item.getStudentName().contains(keyword)
                        || item.getProjectTitle().contains(keyword))
                .toList();
        return PageUtils.fromList(records, page, size);
    }

    @Override
    public InternApplication getByIdOrThrow(Long id) {
        InternApplication application = applicationMapper.selectById(id);
        if (application == null || (application.getDeleted() != null && application.getDeleted() == 1)) {
            throw new BusinessException(404, "申请不存在");
        }
        return application;
    }

    private void authorizeView(InternApplication application) {
        String role = UserContext.getCurrentRole();
        Long currentUserId = UserContext.getCurrentUserId();
        if (RoleEnum.ADMIN.name().equals(role)) {
            return;
        }
        if (RoleEnum.STUDENT.name().equals(role) && application.getStudentId().equals(currentUserId)) {
            return;
        }
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        if (RoleEnum.TEACHER.name().equals(role) && project.getTeacherId().equals(currentUserId)) {
            return;
        }
        throw new BusinessException(403, "无权限查看该申请");
    }

    private void assertTeacherOwner(InternshipProject project) {
        if (!project.getTeacherId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "仅项目教师可操作");
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
