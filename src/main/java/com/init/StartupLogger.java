package com.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupLogger {

    private final Environment environment;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        WebServerApplicationContext context = (WebServerApplicationContext) event.getApplicationContext();
        int port = context.getWebServer().getPort();
        String appName = environment.getProperty("spring.application.name", "application");
        String[] activeProfiles = environment.getActiveProfiles();
        Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();

        log.info("应用启动完成: name={}, profiles={}, port={}", appName, Arrays.toString(activeProfiles), port);
        log.info("本地访问入口: http://localhost:{}/", port);
        log.info("接口文档入口: http://localhost:{}/doc.html", port);
        log.info("上传目录已就绪: {}", uploadPath);
    }
}
