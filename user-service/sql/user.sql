create database user_db;

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
                        `account` varchar(20) NOT NULL COMMENT '账号',
                        `username` varchar(50) DEFAULT NULL COMMENT '用户名（登录名）',
                        `email` varchar(255) DEFAULT NULL COMMENT '邮箱（可选）',
                        `phone` varchar(20) DEFAULT NULL COMMENT '手机号（可选）',
                        `password` varchar(255) NOT NULL COMMENT '加密后的密码',
                        `avatar` varchar(255) DEFAULT 'default_avatar.png' COMMENT '头像路径',
                        `bio` text COMMENT '个人简介',
                        `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户，1-管理员，2-审核员',
                        `is_email_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '邮箱是否验证',
                        `is_phone_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '手机是否验证',
                        `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活账号（0-禁用）',
                        `user_level` tinyint(1) NOT NULL DEFAULT '0' COMMENT '用户等级：1 VIP 用户，0 普通用户 ',
                        `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                        `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
                        `is_delete` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0 未删除，1 被删除。',
                        `deleted_time` datetime DEFAULT NULL COMMENT '逻辑删除时间（软删除）',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `account` (`account`),
                        UNIQUE KEY `email` (`email`),
                        UNIQUE KEY `phone` (`phone`),
                        KEY `idx_username` (`username`),
                        KEY `idx_role` (`role`),
                        KEY `idx_is_active` (`is_active`),
                        KEY `idx_last_login` (`last_login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'hzy','hzy',NULL,NULL,'f1c1e5719bc2027857fad2130db08868','https://tse2-mm.cn.bing.net/th/id/OIP-C.7GLMYPqMlt2LgkbPsOnDIAAAAA?cb=iwc2&rs=1&pid=ImgDetMain',NULL,0,0,0,1,0,'2025-03-14 09:07:41','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(2,'user001','张三','zhangsan@example.com','13800000001','encrypted_password_1','http://example.com/avatar1.png','喜欢旅行和美食',0,1,1,1,0,'2025-03-16 08:30:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(3,'user002','李四','lisi@example.com','13800000002','encrypted_password_2','http://example.com/avatar2.png','热爱编程和科技',1,1,1,1,0,'2025-03-15 10:00:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(4,'user003','王五','wangwu@example.com','13800000003','encrypted_password_3','http://example.com/avatar3.png','喜欢摄影和艺术',0,0,1,1,0,'2025-03-14 12:00:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(5,'user004','赵六','zhaoliu@example.com','13800000004','encrypted_password_4','http://example.com/avatar4.png','音乐爱好者',2,1,0,1,1,'2025-03-13 14:30:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(6,'user005','孙七','sunqi@example.com','13800000005','encrypted_password_5','http://example.com/avatar5.png','运动达人，特别喜欢跑步',0,0,1,1,0,'2025-03-12 11:00:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(7,'user006','周八','zhouba@example.com','13800000006','encrypted_password_6','http://example.com/avatar6.png','时尚潮人',1,1,1,1,1,'2025-03-11 09:00:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(8,'user007','吴九','wujiu@example.com','13800000007','encrypted_password_7','http://example.com/avatar7.png','喜欢写作和阅读',0,1,0,1,0,'2025-03-10 13:30:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(9,'user008','郑十','zhengshi@example.com','13800000008','encrypted_password_8','http://example.com/avatar8.png','科技爱好者，关注未来趋势',1,0,1,1,0,'2025-03-09 15:20:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(10,'user009','冯十一','fengshi@example.com','13800000009','encrypted_password_9','http://example.com/avatar9.png','电影迷，特别喜欢科幻',0,1,1,1,0,'2025-03-08 10:45:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(11,'user010','陈十二','chenshi@example.com','13800000010','encrypted_password_10','http://example.com/avatar10.png','喜欢做饭和手工艺术',0,0,1,1,0,'2025-03-07 17:50:00','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL),(12,'18818324939',NULL,NULL,NULL,'f1c1e5719bc2027857fad2130db08868','default_avatar.png',NULL,0,0,0,1,0,'2025-05-18 19:37:09','2025-05-18 19:37:16','2025-05-18 19:37:16',0,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;