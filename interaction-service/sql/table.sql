create database interaction_db;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
                           `user_id` bigint NOT NULL COMMENT '评论者ID（外键）',
                           `video_id` bigint NOT NULL COMMENT '所属视频ID（外键）',
                           `parent_comment_id` bigint DEFAULT NULL COMMENT '父评论ID（用于回复）',
                           `content` text NOT NULL COMMENT '评论内容',
                           `likes` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
                           `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-审核中，1-已通过，2-已拒绝',
                           `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                           `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                           `deleted_time` datetime DEFAULT NULL COMMENT '逻辑删除时间',
                           `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0-未删除，1-已删除）',
                           `reviewed_time` datetime DEFAULT NULL COMMENT '审核时间',
                           `reviewer_id` bigint DEFAULT NULL COMMENT '审核员ID（外键）',
                           PRIMARY KEY (`id`),
                           KEY `idx_video_id` (`video_id`),
                           KEY `idx_user_id` (`user_id`),
                           KEY `idx_status` (`status`),
                           KEY `idx_parent_comment` (`parent_comment_id`),
                           KEY `idx_created_at` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `video_likes`
--

DROP TABLE IF EXISTS `video_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_likes` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录的唯一ID',
                               `user_id` bigint NOT NULL COMMENT '用户ID，标识点赞的用户',
                               `video_id` bigint NOT NULL COMMENT '视频ID，标识被点赞的视频',
                               `liked_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间，记录用户点赞的时间',
                               `is_active` tinyint(1) DEFAULT '0' COMMENT '是否点赞',
                               `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '是否已删除（0：存在； 1：删除）',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `user_id` (`user_id`,`video_id`) COMMENT '每个用户只能对每个视频点赞一次',
                               KEY `idx_user_video_likes` (`user_id`,`video_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记录用户对视频的点赞行为';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video_likes`
--

LOCK TABLES `video_likes` WRITE;
/*!40000 ALTER TABLE `video_likes` DISABLE KEYS */;
INSERT INTO `video_likes` VALUES (14,1,4,'2025-04-21 13:05:50',1,0),(15,1,16,'2025-04-21 13:05:54',1,1),(16,1,1,'2025-05-16 08:15:43',1,1);
/*!40000 ALTER TABLE `video_likes` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `video_favorites`
--

DROP TABLE IF EXISTS `video_favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_favorites` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏记录的唯一ID',
                                   `user_id` bigint NOT NULL COMMENT '用户ID，标识收藏视频的用户',
                                   `video_id` bigint NOT NULL COMMENT '视频ID，标识被收藏的视频',
                                   `favorited_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间，记录用户收藏的时间',
                                   `is_active` tinyint(1) DEFAULT '1' COMMENT '是否收藏',
                                   `is_delete` tinyint DEFAULT '0' COMMENT '是否删除（0：存在；1：删除）',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `user_id` (`user_id`,`video_id`) COMMENT '每个用户只能对每个视频收藏一次',
                                   KEY `idx_user_video_favorites` (`user_id`,`video_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记录用户对视频的收藏行为';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video_favorites`
--

LOCK TABLES `video_favorites` WRITE;
/*!40000 ALTER TABLE `video_favorites` DISABLE KEYS */;
INSERT INTO `video_favorites` VALUES (1,1,1,'2025-04-21 07:28:27',1,1),(2,1,13,'2025-04-21 07:29:31',1,0),(3,1,2,'2025-04-21 07:29:33',1,0);
/*!40000 ALTER TABLE `video_favorites` ENABLE KEYS */;
UNLOCK TABLES;
