package com.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaRepairInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void repairSchema() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            repairAttachmentSchema(connection, statement);
            repairGradeSchema(connection, statement);
        } catch (SQLException ex) {
            log.warn("数据库结构自检失败，旧库兼容修复可能受影响: {}", ex.getMessage());
        }
    }

    private void repairAttachmentSchema(Connection connection, Statement statement) throws SQLException {
        Set<String> columns = loadColumns(connection, "sys_attachment");
        List<String> statements = new ArrayList<>();
        if (!columns.contains("original_name")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN original_name VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原文件名' AFTER ref_id");
        }
        if (!columns.contains("stored_name")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN stored_name VARCHAR(255) NOT NULL DEFAULT '' COMMENT '存储文件名' AFTER original_name");
        }
        if (!columns.contains("file_size")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN file_size BIGINT NULL COMMENT '文件大小' AFTER file_url");
        }
        if (!columns.contains("content_type")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN content_type VARCHAR(100) NULL COMMENT '文件类型' AFTER file_size");
        }
        if (!columns.contains("create_by")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN create_by BIGINT NULL COMMENT '上传人' AFTER content_type");
        }
        if (!columns.contains("create_time")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER create_by");
        }
        if (!columns.contains("deleted")) {
            statements.add("ALTER TABLE sys_attachment ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除' AFTER create_time");
        }
        for (String sql : statements) {
            statement.execute(sql);
        }
        repairAttachmentIndexes(connection, statement);
        backfillLegacyAttachmentColumns(columns, statement);
        relaxLegacyAttachmentColumns(columns, statement);
        log.info("数据库结构自检完成: sys_attachment");
    }

    private void repairGradeSchema(Connection connection, Statement statement) throws SQLException {
        Set<String> columns = loadColumns(connection, "intern_grade");
        if (columns.contains("project_id")) {
            statement.execute("ALTER TABLE intern_grade MODIFY project_id BIGINT NULL COMMENT '项目ID'");
        }
        log.info("数据库结构自检完成: intern_grade");
    }

    private Set<String> loadColumns(Connection connection, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, null)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME").toLowerCase());
            }
        }
        return columns;
    }

    private void repairAttachmentIndexes(Connection connection, Statement statement) throws SQLException {
        if (!hasIndex(connection, "sys_attachment", "idx_attachment_ref")) {
            statement.execute("CREATE INDEX idx_attachment_ref ON sys_attachment (ref_type, ref_id)");
        }
    }

    private boolean hasIndex(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (resultSet.next()) {
                String current = resultSet.getString("INDEX_NAME");
                if (current != null && current.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void backfillLegacyAttachmentColumns(Set<String> columns, Statement statement) throws SQLException {
        if (columns.contains("file_name")) {
            statement.executeUpdate("UPDATE sys_attachment SET original_name = file_name WHERE original_name = '' OR original_name IS NULL");
        }
        if (columns.contains("file_url")) {
            statement.executeUpdate("UPDATE sys_attachment SET stored_name = SUBSTRING_INDEX(file_url, '/', -1) WHERE stored_name = '' OR stored_name IS NULL");
        }
        if (columns.contains("file_type")) {
            statement.executeUpdate("UPDATE sys_attachment SET content_type = file_type WHERE content_type IS NULL OR content_type = ''");
        }
        if (columns.contains("upload_by")) {
            statement.executeUpdate("UPDATE sys_attachment SET create_by = upload_by WHERE create_by IS NULL");
        }
    }

    private void relaxLegacyAttachmentColumns(Set<String> columns, Statement statement) throws SQLException {
        if (columns.contains("file_name")) {
            statement.execute("ALTER TABLE sys_attachment MODIFY file_name VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原始文件名'");
        }
        if (columns.contains("upload_by")) {
            statement.execute("ALTER TABLE sys_attachment MODIFY upload_by BIGINT NULL COMMENT '上传者ID'");
        }
    }
}
