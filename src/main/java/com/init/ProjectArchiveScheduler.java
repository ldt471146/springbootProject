package com.init;

import com.service.InternshipProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectArchiveScheduler {

    private final InternshipProjectService projectService;

    @Value("${app.project-auto-archive.enabled:true}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void archiveOnStartup() {
        runArchive("startup", true);
    }

    @Scheduled(cron = "${app.project-auto-archive.cron:0 0/30 * * * ?}")
    public void archiveOnSchedule() {
        runArchive("scheduled", false);
    }

    private void runArchive(String trigger, boolean logWhenEmpty) {
        if (!enabled) {
            if (logWhenEmpty) {
                log.info("项目自动归档已关闭: trigger={}", trigger);
            }
            return;
        }
        int archivedCount = projectService.autoArchiveExpiredProjects();
        if (archivedCount > 0 || logWhenEmpty) {
            log.info("项目自动归档扫描完成: trigger={}, archivedCount={}", trigger, archivedCount);
        }
    }
}
