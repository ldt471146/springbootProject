package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.PageResult;
import com.dto.JournalDTO;
import com.dto.JournalReviewDTO;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.entity.Journal;
import com.exception.BusinessException;
import com.mapper.JournalMapper;
import com.security.UserContext;
import com.service.ApplicationService;
import com.service.InternshipProjectService;
import com.service.JournalService;
import com.service.support.ViewAssembler;
import com.vo.JournalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalMapper journalMapper;
    private final ApplicationService applicationService;
    private final InternshipProjectService projectService;
    private final ViewAssembler viewAssembler;

    @Override
    public JournalVO create(JournalDTO dto) {
        InternApplication application = applicationService.getByIdOrThrow(dto.getApplicationId());
        assertStudentOwner(application);
        Journal journal = new Journal();
        fillJournal(journal, dto);
        journal.setStudentId(UserContext.getCurrentUserId());
        journalMapper.insert(journal);
        return viewAssembler.toJournalVO(journal);
    }

    @Override
    public JournalVO update(Long id, JournalDTO dto) {
        Journal journal = getByIdOrThrow(id);
        InternApplication application = applicationService.getByIdOrThrow(journal.getApplicationId());
        assertStudentOwner(application);
        fillJournal(journal, dto);
        journalMapper.updateById(journal);
        return viewAssembler.toJournalVO(journal);
    }

    @Override
    public void delete(Long id) {
        Journal journal = getByIdOrThrow(id);
        InternApplication application = applicationService.getByIdOrThrow(journal.getApplicationId());
        assertStudentOwner(application);
        journalMapper.deleteById(id);
    }

    @Override
    public JournalVO getDetail(Long id) {
        Journal journal = getByIdOrThrow(id);
        authorizeRead(journal);
        return viewAssembler.toJournalVO(journal);
    }

    @Override
    public PageResult<JournalVO> listMy(int page, int size) {
        Page<Journal> pager = new Page<>(page, size);
        Page<Journal> result = journalMapper.selectPage(pager, new LambdaQueryWrapper<Journal>()
                .eq(Journal::getStudentId, UserContext.getCurrentUserId())
                .orderByDesc(Journal::getJournalDate)
                .orderByDesc(Journal::getCreateTime));
        List<JournalVO> records = result.getRecords().stream().map(viewAssembler::toJournalVO).toList();
        return PageResult.of(result, records);
    }

    @Override
    public PageResult<JournalVO> listByApplication(Long appId, int page, int size) {
        InternApplication application = applicationService.getByIdOrThrow(appId);
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        Page<Journal> pager = new Page<>(page, size);
        Page<Journal> result = journalMapper.selectPage(pager, new LambdaQueryWrapper<Journal>()
                .eq(Journal::getApplicationId, appId)
                .orderByDesc(Journal::getJournalDate)
                .orderByDesc(Journal::getCreateTime));
        List<JournalVO> records = result.getRecords().stream().map(viewAssembler::toJournalVO).toList();
        return PageResult.of(result, records);
    }

    @Override
    public List<JournalVO> timeline(Long appId) {
        InternApplication application = applicationService.getByIdOrThrow(appId);
        authorizeReadByApplication(application);
        return journalMapper.selectList(new LambdaQueryWrapper<Journal>()
                        .eq(Journal::getApplicationId, appId)
                        .orderByAsc(Journal::getJournalDate)
                        .orderByAsc(Journal::getCreateTime))
                .stream()
                .map(viewAssembler::toJournalVO)
                .toList();
    }

    @Override
    public JournalVO review(Long id, JournalReviewDTO dto) {
        Journal journal = getByIdOrThrow(id);
        InternApplication application = applicationService.getByIdOrThrow(journal.getApplicationId());
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        assertTeacherOwner(project);
        journal.setTeacherComment(dto.getTeacherComment());
        journal.setScore(dto.getScore());
        journal.setScoredAt(LocalDateTime.now());
        journalMapper.updateById(journal);
        return viewAssembler.toJournalVO(journal);
    }

    private Journal getByIdOrThrow(Long id) {
        Journal journal = journalMapper.selectById(id);
        if (journal == null || (journal.getDeleted() != null && journal.getDeleted() == 1)) {
            throw new BusinessException(404, "日志不存在");
        }
        return journal;
    }

    private void fillJournal(Journal journal, JournalDTO dto) {
        journal.setApplicationId(dto.getApplicationId());
        journal.setTitle(dto.getTitle());
        journal.setContent(dto.getContent());
        journal.setJournalType(dto.getJournalType().name());
        journal.setJournalDate(dto.getJournalDate());
        journal.setWeekNo(dto.getWeekNo());
    }

    private void assertStudentOwner(InternApplication application) {
        if (!application.getStudentId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "只能操作自己的日志");
        }
    }

    private void assertTeacherOwner(InternshipProject project) {
        if (!project.getTeacherId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException(403, "仅指导教师可操作");
        }
    }

    private void authorizeRead(Journal journal) {
        InternApplication application = applicationService.getByIdOrThrow(journal.getApplicationId());
        authorizeReadByApplication(application);
    }

    private void authorizeReadByApplication(InternApplication application) {
        if (application.getStudentId().equals(UserContext.getCurrentUserId())) {
            return;
        }
        InternshipProject project = projectService.getByIdOrThrow(application.getProjectId());
        if (project.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return;
        }
        throw new BusinessException(403, "无权限查看日志");
    }
}
