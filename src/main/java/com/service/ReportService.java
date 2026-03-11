package com.service;

import com.common.PageResult;
import com.dto.ReportDTO;
import com.dto.ReportReviewDTO;
import com.entity.Report;
import com.vo.AttachmentVO;
import com.vo.ReportVO;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {

    ReportVO create(ReportDTO dto);

    ReportVO update(Long id, ReportDTO dto);

    ReportVO getDetail(Long id);

    PageResult<ReportVO> listMy(int page, int size);

    PageResult<ReportVO> teacherList(int page, int size, String status);

    ReportVO review(Long id, ReportReviewDTO dto);

    AttachmentVO upload(MultipartFile file);

    Report getByIdOrThrow(Long id);
}
