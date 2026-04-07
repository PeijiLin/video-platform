create database video_db;
DROP TABLE IF EXISTS `video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '视频唯一ID',
                         `user_id` bigint NOT NULL COMMENT '发布者ID（外键）',
                         `title` varchar(255) NOT NULL COMMENT '视频标题',
                         `description` text COMMENT '视频描述',
                         `cover_url` varchar(255) NOT NULL COMMENT '封面图URL',
                         `video_url` varchar(255) NOT NULL COMMENT '视频存储路径（如云存储）',
                         `duration` int NOT NULL COMMENT '视频时长（秒）',
                         `category_id` bigint NOT NULL COMMENT '分类ID（外键）',
                         `tags` varchar(255) DEFAULT NULL COMMENT '标签（逗号分隔，如：#旅行,#美食）',
                         `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-审核中，1-已通过，2-已拒绝',
                         `views` bigint NOT NULL DEFAULT '0' COMMENT '播放量',
                         `likes` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
                         `collections` bigint NOT NULL DEFAULT '0' COMMENT '收藏数',
                         `comments` bigint NOT NULL DEFAULT '0' COMMENT '评论数',
                         `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                         `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                         `reviewed_time` datetime DEFAULT NULL COMMENT '审核时间',
                         `reviewer_id` bigint DEFAULT NULL COMMENT '审核员ID（外键）',
                         `deleted_time` datetime DEFAULT NULL COMMENT '逻辑删除时间',
                         `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0-未删除，1-删除）',
                         PRIMARY KEY (`id`),
                         KEY `idx_user_id` (`user_id`),
                         KEY `idx_category_id` (`category_id`),
                         KEY `idx_status` (`status`),
                         KEY `idx_created_time` (`created_time`),
                         KEY `idx_views` (`views`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video`
--

LOCK TABLES `video` WRITE;
/*!40000 ALTER TABLE `video` DISABLE KEYS */;
INSERT INTO `video` VALUES (1,1,'旅行日记：夏日的海滩','带你走遍世界的美丽海滩，感受夏日的清凉','https://file.psd.cn/p/stock/20221014/f1lj5pww4yo.jpg','http://vodpub6.v.news.cn/yqfbzx-original/20230818/20230818bd5de7446029436cb41e0ff2c37e1c54_606f38a617a746988c4bb396538066dd.mp4',180,1,'#旅行,#海滩',1,10000,0,0,100,'2025-03-16 10:00:00','2025-05-16 16:15:44','2025-03-16 10:05:00',2,NULL,0),(2,2,'美食探秘：正宗意大利披萨制作','学习如何制作传统的意大利披萨，感受地道的美味','https://img.ixintu.com/download/jpg/201911/e25b904bc42a74d7d77aed81e66d772c.jpg!con','http://example.com/video2.mp4',300,3,'#美食,#披萨',1,15000,800,0,200,'2025-03-15 15:30:00','2025-05-15 20:30:45','2025-03-15 15:35:00',3,NULL,0),(3,3,'健身教程：塑形腹肌训练','强力腹肌训练技巧，助你塑造完美身材','https://img.shetu66.com/2023/06/27/1687858946411822.png','http://example.com/video3.mp4',240,4,'#健身,#腹肌',1,20000,1000,0,300,'2025-03-14 12:00:00','2025-05-15 20:30:45','2025-03-14 12:10:00',4,NULL,0),(4,4,'音乐演奏：钢琴曲《夜曲》','一首感人至深的钢琴夜曲演奏','https://bpic.588ku.com/element_origin_min_pic/23/07/11/d32dabe266d10da8b21bd640a2e9b611.jpg!r650','http://example.com/video4.mp4',360,2,'#音乐,#钢琴',1,5000,1,0,50,'2025-03-13 18:45:00','2025-05-15 20:30:45','2025-03-13 18:50:00',5,NULL,0),(5,5,'电影剪辑：最经典的战斗场面','电影中最经典的战斗场景合集','https://img.shetu66.com/2023/04/25/1682391069844152.png','http://example.com/video5.mp4',480,1,'#电影,#战斗',1,8000,400,0,120,'2025-03-12 14:30:00','2025-05-15 20:30:45','2025-03-12 14:35:00',6,NULL,0),(6,6,'科技揭秘：未来的无人驾驶','探索未来无人驾驶的技术与发展趋势','https://img.shetu66.com/2023/06/28/1687937373741115.png','http://example.com/video6.mp4',420,1,'#科技,#无人驾驶',1,12000,600,0,150,'2025-03-11 09:00:00','2025-05-15 20:30:45','2025-03-11 09:10:00',7,NULL,0),(7,7,'摄影教程：光影与构图','通过光影与构图提升你的摄影技巧','https://pic1.zhimg.com/v2-02760a1bf058904006740d3f66b2c9ac_r.jpg?source=1940ef5c','http://example.com/video7.mp4',200,1,'#摄影,#构图',1,7000,350,0,80,'2025-03-10 11:20:00','2025-05-15 20:30:45','2025-03-10 11:25:00',8,NULL,0),(8,8,'游戏实况：王者荣耀高手对决','展示顶级玩家的游戏实况，精彩对决不断','https://img.shetu66.com/2023/06/21/1687311446374252.png','http://example.com/video8.mp4',600,108,'#游戏,#王者荣耀',1,30000,1500,0,500,'2025-03-09 17:40:00','2025-03-17 11:05:35','2025-03-09 17:45:00',9,NULL,0),(9,9,'极限运动：滑板花样表演','滑板高手的惊险表演，挑战极限','https://ts1.tc.mm.bing.net/th/id/R-C.2a49d9f3677d83a9a2d8849f1442bb22?rik=DpSAj9AUyR4RrA&riu=http%3a%2f%2ffile.51pptmoban.com%2fd%2ffile%2f2023%2f06%2f04%2fb3925630992729172938c08655e5cfd0.jpg&ehk=avAdUGiVQ52VCb2qjEv6sors6E%2bzLS0DO4tv4hnl6aw%3d&risl=&pid','http://example.com/video9.mp4',180,109,'#极限运动,#滑板',1,6000,250,0,70,'2025-03-08 13:00:00','2025-03-17 11:05:35','2025-03-08 13:10:00',10,NULL,0),(10,10,'宠物日常：可爱猫咪视频','记录猫咪的一天，萌到爆炸','https://ts1.tc.mm.bing.net/th/id/R-C.1533990ac167307a9e41a26264ff60c0?rik=BdnfKn%2bzr6s9LA&riu=http%3a%2f%2fpic.616pic.com%2fys_bnew_img%2f00%2f02%2f01%2fVafZfftlPM.jpg&ehk=oYaJTgFJKZf2M0gW74jKzZPB%2bWIOojeg0AF9DZS3nLU%3d&risl=&pid=ImgRaw&r=0','http://example.com/video10.mp4',120,110,'#宠物,#猫咪',1,9000,450,0,90,'2025-03-07 16:30:00','2025-03-17 11:05:35','2025-03-07 16:35:00',11,NULL,0),(11,1,'供奉的是','和广泛的换个地方','http://suli4jqgf.hn-bkt.clouddn.com/fc574a54-4d82-4d36-afe1-28935cad2d12.jpg?e=1744532142&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:3UZ9VCN4Y3NIQwzJHkS1zw0hpJ4=','http://suli4jqgf.hn-bkt.clouddn.com/310b9f81-f92a-4aae-9a60-76f8f9de6329.mp4?e=1744532136&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:VnFFMi3AjhA8tCvDLSxqMXiHw4E=',46,3,'告诉对方,给对方撒',1,0,0,0,0,'2025-04-13 15:15:45','2025-04-19 15:07:39',NULL,NULL,NULL,0),(12,1,'若依','ai','http://suli4jqgf.hn-bkt.clouddn.com/501b4584-a5f0-4456-9c6e-e73b54127981.jpg?e=1745053308&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:7CEOJyLqf2E5W12zTik5pDI4bWw=','http://suli4jqgf.hn-bkt.clouddn.com/4cedccd0-4fc1-44aa-b2f8-fb75c0b59eb9.mp4?e=1745053307&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:aqaNn96WYGyKN7d8YBApviW5yac=',46,9,'Java',1,0,0,0,0,'2025-04-19 16:01:48','2025-04-19 16:02:19',NULL,NULL,NULL,0),(13,1,'旅行日记：夏日的海滩','带你走遍世界的美丽海滩，感受夏日的清凉','https://file.psd.cn/p/stock/20221014/f1lj5pww4yo.jpg','http://vodpub6.v.news.cn/yqfbzx-original/20230818/20230818bd5de7446029436cb41e0ff2c37e1c54_606f38a617a746988c4bb396538066dd.mp4',180,101,'#旅行,#海滩',1,10000,0,0,100,'2025-03-16 10:00:00','2025-04-21 20:35:09','2025-03-16 10:05:00',2,NULL,0),(14,2,'美食探秘：正宗意大利披萨制作','学习如何制作传统的意大利披萨，感受地道的美味','https://img.ixintu.com/download/jpg/201911/e25b904bc42a74d7d77aed81e66d772c.jpg!con','http://example.com/video2.mp4',300,102,'#美食,#披萨',1,15000,1,0,200,'2025-03-15 15:30:00','2025-04-21 20:39:43','2025-03-15 15:35:00',3,NULL,0),(15,3,'健身教程：塑形腹肌训练','强力腹肌训练技巧，助你塑造完美身材','https://img.shetu66.com/2023/06/27/1687858946411822.png','http://example.com/video3.mp4',240,103,'#健身,#腹肌',1,20000,1000,0,300,'2025-03-14 12:00:00','2025-03-17 11:05:35','2025-03-14 12:10:00',4,NULL,0),(16,4,'音乐演奏：钢琴曲《夜曲》','一首感人至深的钢琴夜曲演奏','https://bpic.588ku.com/element_origin_min_pic/23/07/11/d32dabe266d10da8b21bd640a2e9b611.jpg!r650','http://example.com/video4.mp4',360,104,'#音乐,#钢琴',1,5000,0,0,50,'2025-03-13 18:45:00','2025-04-21 21:09:25','2025-03-13 18:50:00',5,NULL,0),(17,5,'电影剪辑：最经典的战斗场面','电影中最经典的战斗场景合集','https://img.shetu66.com/2023/04/25/1682391069844152.png','http://example.com/video5.mp4',480,105,'#电影,#战斗',1,8000,400,0,120,'2025-03-12 14:30:00','2025-03-17 11:05:35','2025-03-12 14:35:00',6,NULL,0),(18,6,'科技揭秘：未来的无人驾驶','探索未来无人驾驶的技术与发展趋势','https://img.shetu66.com/2023/06/28/1687937373741115.png','http://example.com/video6.mp4',420,106,'#科技,#无人驾驶',1,12000,600,0,150,'2025-03-11 09:00:00','2025-03-17 11:05:35','2025-03-11 09:10:00',7,NULL,0),(19,7,'摄影教程：光影与构图','通过光影与构图提升你的摄影技巧','https://pic1.zhimg.com/v2-02760a1bf058904006740d3f66b2c9ac_r.jpg?source=1940ef5c','http://example.com/video7.mp4',200,107,'#摄影,#构图',1,7000,350,0,80,'2025-03-10 11:20:00','2025-03-17 11:05:35','2025-03-10 11:25:00',8,NULL,0),(20,8,'游戏实况：王者荣耀高手对决','展示顶级玩家的游戏实况，精彩对决不断','https://img.shetu66.com/2023/06/21/1687311446374252.png','http://example.com/video8.mp4',600,108,'#游戏,#王者荣耀',1,30000,1500,0,500,'2025-03-09 17:40:00','2025-03-17 11:05:35','2025-03-09 17:45:00',9,NULL,0),(21,9,'极限运动：滑板花样表演','滑板高手的惊险表演，挑战极限','https://ts1.tc.mm.bing.net/th/id/R-C.2a49d9f3677d83a9a2d8849f1442bb22?rik=DpSAj9AUyR4RrA&riu=http%3a%2f%2ffile.51pptmoban.com%2fd%2ffile%2f2023%2f06%2f04%2fb3925630992729172938c08655e5cfd0.jpg&ehk=avAdUGiVQ52VCb2qjEv6sors6E%2bzLS0DO4tv4hnl6aw%3d&risl=&pid','http://example.com/video9.mp4',180,109,'#极限运动,#滑板',1,6000,250,0,70,'2025-03-08 13:00:00','2025-03-17 11:05:35','2025-03-08 13:10:00',10,NULL,0),(22,10,'宠物日常：可爱猫咪视频','记录猫咪的一天，萌到爆炸','https://ts1.tc.mm.bing.net/th/id/R-C.1533990ac167307a9e41a26264ff60c0?rik=BdnfKn%2bzr6s9LA&riu=http%3a%2f%2fpic.616pic.com%2fys_bnew_img%2f00%2f02%2f01%2fVafZfftlPM.jpg&ehk=oYaJTgFJKZf2M0gW74jKzZPB%2bWIOojeg0AF9DZS3nLU%3d&risl=&pid=ImgRaw&r=0','http://example.com/video10.mp4',120,110,'#宠物,#猫咪',1,9000,450,0,90,'2025-03-07 16:30:00','2025-03-17 11:05:35','2025-03-07 16:35:00',11,NULL,0),(23,1,'供奉的是','和广泛的换个地方','http://suli4jqgf.hn-bkt.clouddn.com/fc574a54-4d82-4d36-afe1-28935cad2d12.jpg?e=1744532142&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:3UZ9VCN4Y3NIQwzJHkS1zw0hpJ4=','http://suli4jqgf.hn-bkt.clouddn.com/310b9f81-f92a-4aae-9a60-76f8f9de6329.mp4?e=1744532136&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:VnFFMi3AjhA8tCvDLSxqMXiHw4E=',46,3,'告诉对方,给对方撒',1,0,0,0,0,'2025-04-13 15:15:45','2025-04-19 15:07:39',NULL,NULL,NULL,0),(24,1,'若依','ai','http://suli4jqgf.hn-bkt.clouddn.com/501b4584-a5f0-4456-9c6e-e73b54127981.jpg?e=1745053308&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:7CEOJyLqf2E5W12zTik5pDI4bWw=','http://suli4jqgf.hn-bkt.clouddn.com/4cedccd0-4fc1-44aa-b2f8-fb75c0b59eb9.mp4?e=1745053307&token=TrgjvRrETbHKTlEHWU9ix20_PiBX5z5OI_4XYRl9:aqaNn96WYGyKN7d8YBApviW5yac=',46,9,'Java',1,0,0,0,0,'2025-04-19 16:01:48','2025-04-19 16:02:19',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `video` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `name` varchar(50) NOT NULL COMMENT '分类名称',
                            `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除（0-未删除，1-删除）',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'电影','2025-03-21 16:19:31',0),(2,'电视剧','2025-03-21 16:19:31',0),(3,'纪录片','2025-03-21 16:19:31',0),(4,'动漫','2025-03-21 16:19:31',0),(5,'综艺','2025-03-21 16:19:31',0),(6,'短片','2025-03-21 16:19:31',0),(7,'音乐','2025-03-21 16:19:31',0),(8,'教育','2025-03-21 16:19:31',0),(9,'科技','2025-03-21 16:19:31',0),(10,'游戏','2025-03-21 16:19:31',0);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;