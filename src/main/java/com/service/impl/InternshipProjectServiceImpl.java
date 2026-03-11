package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.PageResult;
import com.common.enums.ApprovalStatus;
import com.common.enums.ProjectStatus;
import com.common.enums.RoleEnum;
import com.dto.ProjectDTO;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.exception.BusinessException;
import com.mapper.ApplicationMapper;
import com.mapper.InternshipProjectMapper;
import com.security.UserContext;
import com.service.InternshipProjectService;
import com.service.UserService;
import com.service.support.ViewAssembler;
import com.vo.ProjectVO;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipProjectServiceImpl implements InternshipProjectService {

    private final InternshipProjectMapper projectMapper;
    private final ApplicationMapper applicationMapper;
    private final UserService userService;
    private final ViewAssembler viewAssembler;

    @Override
    public ProjectVO create(ProjectDTO dto) {
        InternshipProject project = new InternshipProject();
        fillProject(project, dto);
        project.setTeacherId(UserContext.getCurrentUserId());
        project.setStatus(dto.getStatus() == null ? ProjectStatus.OPEN.name() : dto.getStatus().name());
        projectMapper.insert(project);
        return viewAssembler.toProjectVO(project, 0);
    }

    @Override
    public ProjectVO update(Long id, ProjectDTO dto) {
        InternshipProject project = getByIdOrThrow(id);
        assertProjectOwner(project);
        fillProject(project, dto);
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus().name());
        }
        projectMapper.updateById(project);
        return viewAssembler.toProjectVO(project, (int) countApprovedParticipants(project.getId()));
    }

    @Override
    public void delete(Long id) {
        InternshipProject project = getByIdOrThrow(id);
        assertProjectOwner(project);
        projectMapper.deleteById(project.getId());
    }

    @Override
    public ProjectVO getDetail(Long id) {
        InternshipProject project = getByIdOrThrow(id);
        return viewAssembler.toProjectVO(project, (int) countApprovedParticipants(id));
    }

    @Override
    public PageResult<ProjectVO> listOpenProjects(int page, int size, String keyword, String college, String grade) {
        Page<InternshipProject> pager = new Page<>(page, size);
        LambdaQueryWrapper<InternshipProject> wrapper = new LambdaQueryWrapper<InternshipProject>()
                .eq(InternshipProject::getStatus, ProjectStatus.OPEN.name())
                .orderByDesc(InternshipProject::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(InternshipProject::getTitle, keyword)
                    .or().like(InternshipProject::getCompany, keyword)
                    .or().like(InternshipProject::getDescription, keyword));
        }
        if (college != null && !college.isBlank()) {
            wrapper.and(w -> w.isNull(InternshipProject::getCollegeLimit).or().eq(InternshipProject::getCollegeLimit, college));
        }
        if (grade != null && !grade.isBlank()) {
            wrapper.and(w -> w.isNull(InternshipProject::getGradeLimit).or().eq(InternshipProject::getGradeLimit, grade));
        }
        Page<InternshipProject> result = projectMapper.selectPage(pager, wrapper);
        List<ProjectVO> records = result.getRecords().stream()
                .map(project -> viewAssembler.toProjectVO(project, (int) countApprovedParticipants(project.getId())))
                .toList();
        return PageResult.of(result, records);
    }

    @Override
    public PageResult<ProjectVO> listMyProjects(int page, int size) {
        Page<InternshipProject> pager = new Page<>(page, size);
        Page<InternshipProject> result = projectMapper.selectPage(pager, new LambdaQueryWrapper<InternshipProject>()
                .eq(InternshipProject::getTeacherId, UserContext.getCurrentUserId())
                .orderByDesc(InternshipProject::getCreateTime));
        List<ProjectVO> records = result.getRecords().stream()
                .map(project -> viewAssembler.toProjectVO(project, (int) countApprovedParticipants(project.getId())))
                .toList();
        return PageResult.of(result, records);
    }

    @Override
    public void close(Long id) {
        InternshipProject project = getByIdOrThrow(id);
        assertProjectOwner(project);
        project.setStatus(ProjectStatus.CLOSED.name());
        projectMapper.updateById(project);
    }

    @Override
    public void archive(Long id) {
        InternshipProject project = getByIdOrThrow(id);
        assertProjectOwner(project);
        project.setStatus(ProjectStatus.ARCHIVED.name());
        projectMapper.updateById(project);
    }

    @Override
    public PageResult<UserVO> listParticipants(Long projectId, int page, int size) {
        InternshipProject project = getByIdOrThrow(projectId);
        assertProjectOwner(project);
        Page<InternApplication> pager = new Page<>(page, size);
        Page<InternApplication> result = applicationMapper.selectPage(pager, new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getProjectId, projectId)
                .eq(InternApplication::getStatus, ApprovalStatus.APPROVED.name())
                .orderByDesc(InternApplication::getCreateTime));
        List<UserVO> records = result.getRecords().stream()
                .map(item -> userService.getByIdOrThrow(item.getStudentId()))
                .map(viewAssembler::toUserVO)
                .toList();
        return PageResult.of(result, records);
    }

    @Override
    public InternshipProject getByIdOrThrow(Long id) {
        InternshipProject project = projectMapper.selectById(id);
        if (project == null || (project.getDeleted() != null && project.getDeleted() == 1)) {
            throw new BusinessException(404, "项目不存在");
        }
        return project;
    }

    @Override
    public long countApprovedParticipants(Long projectId) {
        Long count = applicationMapper.selectCount(new LambdaQueryWrapper<InternApplication>()
                .eq(InternApplication::getProjectId, projectId)
                .eq(InternApplication::getStatus, ApprovalStatus.APPROVED.name()));
        return count == null ? 0L : count;
    }

    private void fillProject(InternshipProject project, ProjectDTO dto) {
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setCompany(dto.getCompany());
        project.setLocation(dto.getLocation());
        project.setMaxStudents(dto.getMaxStudents() == null ? 0 : dto.getMaxStudents());
        project.setCollegeLimit(dto.getCollegeLimit());
        project.setGradeLimit(dto.getGradeLimit());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setEnrollDeadline(dto.getEnrollDeadline());
        project.setSemester(dto.getSemester());
        project.setCreditType(dto.getCreditType());
    }

    private void assertProjectOwner(InternshipProject project) {
        Long currentUserId = UserContext.getCurrentUserId();
        String role = UserContext.getCurrentRole();
        if (RoleEnum.ADMIN.name().equals(role)) {
            return;
        }
        if (currentUserId == null || !currentUserId.equals(project.getTeacherId())) {
            throw new BusinessException(403, "仅项目创建者可操作");
        }
    }
}
