package com.service.support;

import com.entity.Attachment;
import com.entity.Grade;
import com.entity.InternApplication;
import com.entity.InternshipProject;
import com.entity.Journal;
import com.entity.Report;
import com.entity.User;
import com.mapper.ApplicationMapper;
import com.mapper.InternshipProjectMapper;
import com.mapper.UserMapper;
import com.vo.ApplicationVO;
import com.vo.AttachmentVO;
import com.vo.GradeVO;
import com.vo.JournalVO;
import com.vo.ProjectVO;
import com.vo.ReportVO;
import com.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewAssembler {

    private final UserMapper userMapper;
    private final InternshipProjectMapper projectMapper;
    private final ApplicationMapper applicationMapper;

    public UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo, "password", "deleted", "updateTime");
        return vo;
    }

    public ProjectVO toProjectVO(InternshipProject project, int participantCount) {
        if (project == null) {
            return null;
        }
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo, "deleted", "updateTime");
        vo.setParticipantCount(participantCount);
        User teacher = userMapper.selectById(project.getTeacherId());
        vo.setTeacherName(teacher == null ? "-" : teacher.getRealName());
        return vo;
    }

    public ApplicationVO toApplicationVO(InternApplication application) {
        if (application == null) {
            return null;
        }
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(application, vo, "deleted", "updateTime");
        InternshipProject project = projectMapper.selectById(application.getProjectId());
        User student = userMapper.selectById(application.getStudentId());
        User reviewer = application.getReviewedBy() == null ? null : userMapper.selectById(application.getReviewedBy());
        vo.setProjectTitle(project == null ? "-" : project.getTitle());
        vo.setStudentName(student == null ? "-" : student.getRealName());
        vo.setStudentNo(student == null ? null : student.getStudentNo());
        vo.setReviewedByName(reviewer == null ? null : reviewer.getRealName());
        return vo;
    }

    public JournalVO toJournalVO(Journal journal) {
        if (journal == null) {
            return null;
        }
        JournalVO vo = new JournalVO();
        BeanUtils.copyProperties(journal, vo, "deleted", "updateTime");
        InternApplication application = applicationMapper.selectById(journal.getApplicationId());
        InternshipProject project = application == null ? null : projectMapper.selectById(application.getProjectId());
        User student = userMapper.selectById(journal.getStudentId());
        vo.setProjectTitle(project == null ? "-" : project.getTitle());
        vo.setStudentName(student == null ? "-" : student.getRealName());
        return vo;
    }

    public ReportVO toReportVO(Report report) {
        if (report == null) {
            return null;
        }
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(report, vo, "deleted", "updateTime");
        InternApplication application = applicationMapper.selectById(report.getApplicationId());
        InternshipProject project = application == null ? null : projectMapper.selectById(application.getProjectId());
        User student = userMapper.selectById(report.getStudentId());
        vo.setProjectTitle(project == null ? "-" : project.getTitle());
        vo.setStudentName(student == null ? "-" : student.getRealName());
        return vo;
    }

    public GradeVO toGradeVO(Grade grade) {
        if (grade == null) {
            return null;
        }
        GradeVO vo = new GradeVO();
        BeanUtils.copyProperties(grade, vo, "deleted", "updateTime");
        InternApplication application = applicationMapper.selectById(grade.getApplicationId());
        InternshipProject project = application == null ? null : projectMapper.selectById(application.getProjectId());
        User student = userMapper.selectById(grade.getStudentId());
        User teacher = userMapper.selectById(grade.getTeacherId());
        vo.setProjectTitle(project == null ? "-" : project.getTitle());
        vo.setStudentName(student == null ? "-" : student.getRealName());
        vo.setTeacherName(teacher == null ? "-" : teacher.getRealName());
        return vo;
    }

    public AttachmentVO toAttachmentVO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        AttachmentVO vo = new AttachmentVO();
        BeanUtils.copyProperties(attachment, vo);
        return vo;
    }
}
