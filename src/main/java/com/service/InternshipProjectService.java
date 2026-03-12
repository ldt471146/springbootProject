package com.service;

import com.common.PageResult;
import com.dto.ProjectDTO;
import com.entity.InternshipProject;
import com.vo.ProjectVO;
import com.vo.UserVO;

public interface InternshipProjectService {

    ProjectVO create(ProjectDTO dto);

    ProjectVO update(Long id, ProjectDTO dto);

    void delete(Long id);

    ProjectVO getDetail(Long id);

    PageResult<ProjectVO> listOpenProjects(int page, int size, String keyword, String college, String grade);

    PageResult<ProjectVO> listMyProjects(int page, int size);

    void close(Long id);

    void archive(Long id);

    PageResult<UserVO> listParticipants(Long projectId, int page, int size);

    InternshipProject getByIdOrThrow(Long id);

    long countApprovedParticipants(Long projectId);

    int autoArchiveExpiredProjects();
}
