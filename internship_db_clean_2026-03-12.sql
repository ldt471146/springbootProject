-- MySQL dump 10.13  Distrib 8.0.27, for Win64 (x86_64)
--
-- Host: localhost    Database: internship_db
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `internship_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `internship_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `internship_db`;

--
-- Table structure for table `intern_application`
--

DROP TABLE IF EXISTS `intern_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `intern_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id` bigint NOT NULL COMMENT '关联项目ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID',
  `apply_reason` text COMMENT '申请理由',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态: PENDING / APPROVED / REJECTED',
  `review_comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审批人ID',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审批时间',
  `phase` varchar(20) NOT NULL DEFAULT 'ENROLLED' COMMENT '实习阶段: ENROLLED / IN_PROGRESS / COMPLETED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_student` (`project_id`,`student_id`),
  KEY `idx_student` (`student_id`),
  CONSTRAINT `fk_app_project` FOREIGN KEY (`project_id`) REFERENCES `intern_project` (`id`),
  CONSTRAINT `fk_app_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实习申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `intern_application`
--

LOCK TABLES `intern_application` WRITE;
/*!40000 ALTER TABLE `intern_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `intern_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `intern_grade`
--

DROP TABLE IF EXISTS `intern_grade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `intern_grade` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `application_id` bigint NOT NULL COMMENT '关联申请ID(唯一)',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `journal_score` decimal(5,2) DEFAULT NULL COMMENT '日志综合得分',
  `report_score` decimal(5,2) DEFAULT NULL COMMENT '报告得分',
  `final_score` decimal(5,2) DEFAULT NULL COMMENT '最终总评成绩',
  `teacher_id` bigint DEFAULT NULL COMMENT '评定教师ID',
  `teacher_comment` varchar(500) DEFAULT NULL COMMENT '教师综合评语',
  `admin_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '管理员审核: PENDING / CONFIRMED / RETURNED',
  `admin_comment` varchar(500) DEFAULT NULL COMMENT '管理员意见',
  `confirmed_by` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `confirmed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application` (`application_id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_project` (`project_id`),
  CONSTRAINT `fk_grade_app` FOREIGN KEY (`application_id`) REFERENCES `intern_application` (`id`),
  CONSTRAINT `fk_grade_project` FOREIGN KEY (`project_id`) REFERENCES `intern_project` (`id`),
  CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩评定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `intern_grade`
--

LOCK TABLES `intern_grade` WRITE;
/*!40000 ALTER TABLE `intern_grade` DISABLE KEYS */;
/*!40000 ALTER TABLE `intern_grade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `intern_journal`
--

DROP TABLE IF EXISTS `intern_journal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `intern_journal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `application_id` bigint NOT NULL COMMENT '关联申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `title` varchar(200) NOT NULL COMMENT '日志标题',
  `content` text NOT NULL COMMENT '日志内容',
  `journal_type` varchar(20) NOT NULL DEFAULT 'DAILY' COMMENT '类型: DAILY / WEEKLY / PHASE_SUMMARY',
  `journal_date` date NOT NULL COMMENT '日志日期',
  `week_no` int DEFAULT NULL COMMENT '周次(周报时填写)',
  `teacher_comment` varchar(1000) DEFAULT NULL COMMENT '教师批注',
  `score` decimal(5,2) DEFAULT NULL COMMENT '教师评分(百分制)',
  `scored_at` datetime DEFAULT NULL COMMENT '评分时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_app` (`application_id`),
  KEY `idx_student` (`student_id`),
  CONSTRAINT `fk_journal_app` FOREIGN KEY (`application_id`) REFERENCES `intern_application` (`id`),
  CONSTRAINT `fk_journal_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实习日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `intern_journal`
--

LOCK TABLES `intern_journal` WRITE;
/*!40000 ALTER TABLE `intern_journal` DISABLE KEYS */;
/*!40000 ALTER TABLE `intern_journal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `intern_project`
--

DROP TABLE IF EXISTS `intern_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `intern_project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) NOT NULL COMMENT '项目标题',
  `description` text COMMENT '项目详细描述',
  `teacher_id` bigint NOT NULL COMMENT '发布教师ID',
  `company` varchar(200) DEFAULT NULL COMMENT '实习单位',
  `location` varchar(200) DEFAULT NULL COMMENT '实习地点',
  `max_students` int NOT NULL DEFAULT '0' COMMENT '最大招收人数(0=不限)',
  `college_limit` varchar(100) DEFAULT NULL COMMENT '限定学院(NULL=不限)',
  `grade_limit` varchar(20) DEFAULT NULL COMMENT '限定年级(NULL=不限)',
  `start_date` date DEFAULT NULL COMMENT '实习开始日期',
  `end_date` date DEFAULT NULL COMMENT '实习结束日期',
  `enroll_deadline` date DEFAULT NULL COMMENT '报名截止日期',
  `semester` varchar(30) DEFAULT NULL COMMENT '所属学期(如2025-2026-1)',
  `credit_type` varchar(50) DEFAULT NULL COMMENT '学分类型(专业实践/毕业实习等)',
  `status` varchar(20) NOT NULL DEFAULT 'OPEN' COMMENT '状态: OPEN / CLOSED / ARCHIVED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_project_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实习项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `intern_project`
--

LOCK TABLES `intern_project` WRITE;
/*!40000 ALTER TABLE `intern_project` DISABLE KEYS */;
/*!40000 ALTER TABLE `intern_project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `intern_report`
--

DROP TABLE IF EXISTS `intern_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `intern_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `application_id` bigint NOT NULL COMMENT '关联申请ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `title` varchar(200) NOT NULL COMMENT '报告标题',
  `summary` text COMMENT '报告摘要',
  `file_url` varchar(500) NOT NULL COMMENT '报告PDF文件路径',
  `semester` varchar(30) DEFAULT NULL COMMENT '绑定学期',
  `credit_type` varchar(50) DEFAULT NULL COMMENT '绑定学分类型',
  `status` varchar(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态: SUBMITTED / APPROVED / REJECTED / EXCELLENT',
  `teacher_comment` varchar(1000) DEFAULT NULL COMMENT '教师评语',
  `teacher_score` decimal(5,2) DEFAULT NULL COMMENT '教师评分',
  `is_excellent` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐优秀报告',
  `ai_flag` varchar(50) DEFAULT NULL COMMENT 'AI初审标记(NORMAL/TEMPLATE_SUSPECTED/TOO_SHORT/MANUAL_REVIEW)',
  `reviewed_by` bigint DEFAULT NULL COMMENT '评阅教师ID',
  `reviewed_at` datetime DEFAULT NULL COMMENT '评阅时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_app` (`application_id`),
  KEY `idx_student` (`student_id`),
  CONSTRAINT `fk_report_app` FOREIGN KEY (`application_id`) REFERENCES `intern_application` (`id`),
  CONSTRAINT `fk_report_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实习报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `intern_report`
--

LOCK TABLES `intern_report` WRITE;
/*!40000 ALTER TABLE `intern_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `intern_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_attachment`
--

DROP TABLE IF EXISTS `sys_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ref_type` varchar(30) NOT NULL COMMENT '关联类型: JOURNAL / REPORT / PROJECT',
  `ref_id` bigint NOT NULL COMMENT '关联记录ID',
  `original_name` varchar(255) NOT NULL DEFAULT '' COMMENT '原文件名',
  `stored_name` varchar(255) NOT NULL DEFAULT '' COMMENT '存储文件名',
  `file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '原始文件名',
  `file_url` varchar(500) NOT NULL COMMENT '存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
  `create_by` bigint DEFAULT NULL COMMENT '上传人',
  `file_type` varchar(50) DEFAULT NULL COMMENT 'MIME类型',
  `upload_by` bigint DEFAULT NULL COMMENT '上传者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ref` (`ref_type`,`ref_id`),
  KEY `idx_attachment_ref` (`ref_type`,`ref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_attachment`
--

LOCK TABLES `sys_attachment` WRITE;
/*!40000 ALTER TABLE `sys_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role` varchar(20) NOT NULL COMMENT '角色: STUDENT / TEACHER / ADMIN',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '账号状态: PENDING / ACTIVE / DISABLED',
  `student_no` varchar(30) DEFAULT NULL COMMENT '学号(学生专属)',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级(如2022级)',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$SmiebOYuJyjWpXjnMHc4Wuu1KQs9AA7FBRo40D/j.q0ecw6ki1l9K','系统管理员','ADMIN','ACTIVE',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-03-11 11:24:27','2026-03-11 11:24:27',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'internship_db'
--

--
-- Dumping routines for database 'internship_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-12 14:47:14
