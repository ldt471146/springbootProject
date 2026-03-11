package com.service;

import com.common.PageResult;
import com.dto.JournalDTO;
import com.dto.JournalReviewDTO;
import com.vo.JournalVO;

import java.util.List;

public interface JournalService {

    JournalVO create(JournalDTO dto);

    JournalVO update(Long id, JournalDTO dto);

    void delete(Long id);

    JournalVO getDetail(Long id);

    PageResult<JournalVO> listMy(int page, int size);

    PageResult<JournalVO> listByApplication(Long appId, int page, int size);

    List<JournalVO> timeline(Long appId);

    JournalVO review(Long id, JournalReviewDTO dto);
}
