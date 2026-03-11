package com.service;

import com.common.PageResult;
import com.dto.GradeConfirmDTO;
import com.dto.GradeDTO;
import com.vo.GradeVO;

import java.util.List;

public interface GradeService {

    GradeVO create(GradeDTO dto);

    GradeVO update(Long id, GradeDTO dto);

    GradeVO getDetail(Long id);

    PageResult<GradeVO> listMy(int page, int size);

    PageResult<GradeVO> listTeacher(int page, int size);

    PageResult<GradeVO> listAdmin(int page, int size, String status, String semester, String creditType);

    GradeVO confirm(Long id, GradeConfirmDTO dto);

    List<GradeVO> exportable(String semester, String creditType);
}
