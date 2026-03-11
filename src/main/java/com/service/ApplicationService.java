package com.service;

import com.common.PageResult;
import com.dto.ApplicationDTO;
import com.dto.ApplicationPhaseDTO;
import com.dto.ApplicationReviewDTO;
import com.entity.InternApplication;
import com.vo.ApplicationVO;

public interface ApplicationService {

    ApplicationVO create(ApplicationDTO dto);

    PageResult<ApplicationVO> listMyApplications(int page, int size);

    ApplicationVO getDetail(Long id);

    void withdraw(Long id);

    PageResult<ApplicationVO> listProjectApplications(Long projectId, int page, int size, String status);

    ApplicationVO review(Long id, ApplicationReviewDTO dto);

    ApplicationVO updatePhase(Long id, ApplicationPhaseDTO dto);

    PageResult<ApplicationVO> listTeacherStudents(int page, int size, String keyword);

    InternApplication getByIdOrThrow(Long id);
}
