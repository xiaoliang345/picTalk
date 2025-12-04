/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : localhost:3306
 Source Schema         : pictalk

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 04/12/2025 19:47:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论人ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
  `reply_to_user_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的目标用户ID（@谁）',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `like_count` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (2, 14, 1969752947225530369, 0, NULL, '不错哟', '2025-11-07 10:54:21', 0);
INSERT INTO `comment` VALUES (3, 14, 1969752947225530369, 2, 1969752947225530369, '一般般', '2025-11-07 10:54:29', 0);
INSERT INTO `comment` VALUES (4, 14, 1976882229613694977, 3, 1969752947225530369, '行帮', '2025-11-07 10:54:55', 0);
INSERT INTO `comment` VALUES (6, 15, 1969752947225530369, 0, NULL, '啊发发😆', '2025-11-07 11:34:10', 0);

-- ----------------------------
-- Table structure for file_share
-- ----------------------------
DROP TABLE IF EXISTS `file_share`;
CREATE TABLE `file_share`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `share_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '取件码（唯一）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上传的文件名（单个）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expires_at` datetime NOT NULL COMMENT '过期时间（created_at + 20分钟）',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上传者IP地址（可选）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_share_code`(`share_code`) USING BTREE,
  INDEX `idx_expires_at`(`expires_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1989940566144151555 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '单文件分享记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_share
-- ----------------------------

-- ----------------------------
-- Table structure for picture
-- ----------------------------
DROP TABLE IF EXISTS `picture`;
CREATE TABLE `picture`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片 url',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片名称',
  `introduction` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签（JSON 数组）',
  `picSize` bigint(20) NULL DEFAULT NULL COMMENT '图片体积',
  `picWidth` int(11) NULL DEFAULT NULL COMMENT '图片宽度',
  `picHeight` int(11) NULL DEFAULT NULL COMMENT '图片高度',
  `picScale` double NULL DEFAULT NULL COMMENT '图片宽高比例',
  `picFormat` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片格式',
  `userId` bigint(20) NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `reviewStatus` int(11) NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
  `reviewMessage` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核信息',
  `reviewerId` bigint(20) NULL DEFAULT NULL COMMENT '审核人 ID',
  `reviewTime` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `thumbnailUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图 url',
  `spaceId` bigint(20) NULL DEFAULT NULL COMMENT '空间 id（为空表示公共空间）',
  `picColor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片主色调',
  `previewUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览图url',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_introduction`(`introduction`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_tags`(`tags`) USING BTREE,
  INDEX `idx_userId`(`userId`) USING BTREE,
  INDEX `idx_reviewStatus`(`reviewStatus`) USING BTREE,
  INDEX `idx_spaceId`(`spaceId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1996512315733721090 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图片' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of picture
-- ----------------------------
INSERT INTO `picture` VALUES (1987696014905638913, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1969752947225530369/6NmUg.png', '白淞鲜汤', NULL, NULL, NULL, 140199, 275, 275, 1, 'png', 1969752947225530369, '2025-11-10 09:37:14', '2025-11-10 09:37:14', '2025-11-10 09:52:15', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 09:37:14', 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1969752947225530369/6NmUg.webp', NULL, '0x417e9d', 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1969752947225530369/6NmUg.webp');
INSERT INTO `picture` VALUES (1987696242228527106, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1969752947225530369/yq57p.png', '派蒙', NULL, NULL, NULL, 40628, 180, 180, 1, 'png', 1969752947225530369, '2025-11-10 09:38:08', '2025-11-10 09:38:08', '2025-11-10 09:52:13', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 09:38:08', 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1969752947225530369/yq57p.webp', NULL, '0x713134', 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1969752947225530369/yq57p.webp');
INSERT INTO `picture` VALUES (1987714941505351681, 'http://www.oxncloud.top/cos/public/1969752947225530369/i5WOK.jpeg', '地图', NULL, '', NULL, 179276, 1242, 700, 1.77, 'jpeg', 1969752947225530369, '2025-11-10 10:52:26', '2025-11-10 10:52:33', '2025-11-10 10:52:32', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 10:52:33', 'http://www.oxncloud.top/cos/public/1969752947225530369/i5WOK.webp', NULL, '0xa1c0df', 'http://www.oxncloud.top/cos/public/1969752947225530369/i5WOK.webp');
INSERT INTO `picture` VALUES (1987728043546341378, 'http://www.oxncloud.top/cos/public/1976882229613694977/trcIl.png', '奶油蘑菇汤', NULL, '', NULL, 79821, 220, 220, 1, 'png', 1976882229613694977, '2025-11-10 11:44:30', '2025-11-10 11:44:33', '2025-11-10 11:44:33', 0, 0, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/trcIl.webp', 1986639745868431362, '0x456363', 'http://www.oxncloud.top/cos/public/1976882229613694977/trcIl.webp');
INSERT INTO `picture` VALUES (1987741652775170049, 'http://www.oxncloud.top/cos/public/1976882229613694977/Mbudd.png', '奶油蘑菇汤', NULL, NULL, NULL, 79821, 220, 220, 1, 'png', 1976882229613694977, '2025-11-10 12:38:35', '2025-11-10 12:38:35', '2025-11-10 12:38:35', 0, 1, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/Mbudd.webp', 1986639745868431362, '0x456363', 'http://www.oxncloud.top/cos/public/1976882229613694977/Mbudd.webp');
INSERT INTO `picture` VALUES (1987806487122591745, 'http://www.oxncloud.top/cos/public/1969752947225530369/emlEx.png', '派蒙', NULL, '', NULL, 7781, 144, 144, 1, 'png', 1969752947225530369, '2025-11-10 16:56:12', '2025-11-10 16:56:18', '2025-11-10 16:56:49', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 16:56:18', 'http://www.oxncloud.top/cos/public/1969752947225530369/emlEx.webp', 1985345402775912449, '0x723639', 'http://www.oxncloud.top/cos/public/1969752947225530369/emlEx.webp');
INSERT INTO `picture` VALUES (1987812444795928577, 'http://www.oxncloud.top/cos/public/1969752947225530369/ezv0P.png', '休闲-咖啡馆-手绘风', NULL, '电脑壁纸', '[]', 1986703, 2500, 1576, 1.59, 'png', 1969752947225530369, '2025-11-10 17:19:53', '2025-11-10 17:25:23', '2025-11-10 17:36:24', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 17:25:23', 'http://www.oxncloud.top/cos/public/1969752947225530369/ezv0P_thumbnailpng', 1985345402775912449, '0x512522', 'http://www.oxncloud.top/cos/public/1969752947225530369/ezv0P.webp');
INSERT INTO `picture` VALUES (1987814722714042369, 'http://www.oxncloud.top/cos/public/1969752947225530369/jHekh.png', '小新', NULL, NULL, NULL, 693765, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-11-10 17:28:56', '2025-11-10 17:28:56', '2025-11-10 17:35:50', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 17:28:56', 'http://www.oxncloud.top/cos/public/1969752947225530369/jHekh_thumbnailpng', 1985345402775912449, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/jHekh.webp');
INSERT INTO `picture` VALUES (1987816664462884865, 'http://www.oxncloud.top/cos/public/1969752947225530369/ekmn7.png', '派蒙', NULL, '', NULL, 7781, 144, 144, 1, 'png', 1969752947225530369, '2025-11-10 17:36:39', '2025-11-10 17:36:41', '2025-11-10 17:36:41', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 17:36:41', 'http://www.oxncloud.top/cos/public/1969752947225530369/ekmn7.webp', 1985345402775912449, '0x723639', 'http://www.oxncloud.top/cos/public/1969752947225530369/ekmn7.webp');
INSERT INTO `picture` VALUES (1987817021406543874, 'http://www.oxncloud.top/cos/public/1969752947225530369/OvEgB.png', '仙跳墙', NULL, '', NULL, 70726, 220, 220, 1, 'png', 1969752947225530369, '2025-11-10 17:38:04', '2025-11-10 17:38:06', '2025-11-10 17:39:10', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 17:38:06', 'http://www.oxncloud.top/cos/public/1969752947225530369/OvEgB.webp', 1985345402775912449, '0x7d5d3f', 'http://www.oxncloud.top/cos/public/1969752947225530369/OvEgB.webp');
INSERT INTO `picture` VALUES (1987817371463155713, 'http://www.oxncloud.top/cos/public/1969752947225530369/9sRRN.png', '小新', NULL, '', '[]', 693765, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-11-10 17:39:27', '2025-11-10 17:39:53', '2025-11-10 17:39:53', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-10 17:39:53', 'http://www.oxncloud.top/cos/public/1969752947225530369/9sRRN_thumbnailpng', 1985345402775912449, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/9sRRN.webp');
INSERT INTO `picture` VALUES (1988097191883366402, 'http://www.oxncloud.top/cos/public/1969752947225530369/UjtCz.png', '【哲风壁纸】AI原神-原神', NULL, '', NULL, 2051388, 3600, 2400, 1.5, 'png', 1969752947225530369, '2025-11-11 12:11:22', '2025-11-11 12:11:51', '2025-11-11 12:14:45', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:11:51', 'http://www.oxncloud.top/cos/public/1969752947225530369/UjtCz_thumbnailpng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/UjtCz.webp');
INSERT INTO `picture` VALUES (1988098083688890369, 'http://www.oxncloud.top/cos/public/1969752947225530369/hlcjG.png', 'logo', 'sdfasf ', '电脑壁纸', '[\"风景\"]', 693765, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-11-11 12:14:54', '2025-11-13 13:17:05', '2025-11-13 13:17:04', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-13 13:17:05', 'http://www.oxncloud.top/cos/public/1969752947225530369/hlcjG_thumbnailpng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/hlcjG.webp');
INSERT INTO `picture` VALUES (1988098536032002050, 'http://www.oxncloud.top/cos/public/1969752947225530369/hqVlj.png', '【哲风壁纸】AI原神-原神', NULL, '', NULL, 2051388, 3600, 2400, 1.5, 'png', 1969752947225530369, '2025-11-11 12:16:42', '2025-11-11 12:16:45', '2025-11-12 10:22:49', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:16:45', 'http://www.oxncloud.top/cos/public/1969752947225530369/hqVlj_thumbnailpng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/hqVlj.webp');
INSERT INTO `picture` VALUES (1988098875619610625, 'http://www.oxncloud.top/cos/public/1969752947225530369/9Kdop.png', '【哲风壁纸】AI原神-原神', NULL, '', NULL, 2051388, 3600, 2400, 1.5, 'png', 1969752947225530369, '2025-11-11 12:18:03', '2025-11-11 12:18:06', '2025-11-12 10:22:48', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:18:06', 'http://www.oxncloud.top/cos/public/1969752947225530369/9Kdoppng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/9Kdop_thumbnailpng');
INSERT INTO `picture` VALUES (1988100347598016513, 'http://www.oxncloud.top/cos/public/1969752947225530369/pCyoh.png', '【哲风壁纸】刘浩存-女明星', NULL, '', NULL, 1295419, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-11-11 12:23:54', '2025-11-11 12:23:57', '2025-11-12 10:22:47', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:23:57', 'http://www.oxncloud.top/cos/public/1969752947225530369/pCyohpng', NULL, '0x41462e', 'http://www.oxncloud.top/cos/public/1969752947225530369/pCyoh_thumbnailpng');
INSERT INTO `picture` VALUES (1988100696169844737, 'http://www.oxncloud.top/cos/public/1969752947225530369/Oet9B.png', '【哲风壁纸】刘浩存-女明星', NULL, NULL, NULL, 1295419, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-11-11 12:25:17', '2025-11-11 12:25:17', '2025-11-12 10:22:47', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:25:18', 'http://www.oxncloud.top/cos/public/1969752947225530369/Oet9Bpng', NULL, '0x41462e', 'http://www.oxncloud.top/cos/public/1969752947225530369/Oet9B_thumbnailpng');
INSERT INTO `picture` VALUES (1988102369084723201, 'http://www.oxncloud.top/cos/public/1969752947225530369/j3fR0.png', '【哲风壁纸】刘浩存-女明星', NULL, '', NULL, 1295419, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-11-11 12:31:56', '2025-11-11 12:32:00', '2025-11-12 10:22:46', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:32:00', 'http://www.oxncloud.top/cos/public/1969752947225530369/j3fR0png', NULL, '0x41462e', 'http://www.oxncloud.top/cos/public/1969752947225530369/j3fR0_thumbnailpng');
INSERT INTO `picture` VALUES (1988102756978151425, 'http://www.oxncloud.top/cos/public/1969752947225530369/ytOMD.png', '【哲风壁纸】AI原神-原神', NULL, '', NULL, 2051388, 3600, 2400, 1.5, 'png', 1969752947225530369, '2025-11-11 12:33:29', '2025-11-11 12:33:51', '2025-11-12 10:22:46', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:33:51', 'http://www.oxncloud.top/cos/public/1969752947225530369/ytOMDpng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/ytOMD_thumbnailpng');
INSERT INTO `picture` VALUES (1988103392658513921, 'http://www.oxncloud.top/cos/public/1969752947225530369/OXNg9.png', '【哲风壁纸】AI原神-原神', NULL, '', NULL, 2051388, 3600, 2400, 1.5, 'png', 1969752947225530369, '2025-11-11 12:36:00', '2025-11-11 12:36:04', '2025-11-12 10:22:45', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 12:36:04', 'http://www.oxncloud.top/cos/public/1969752947225530369/OXNg9_thumbnailpng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/OXNg9.webp');
INSERT INTO `picture` VALUES (1988104318496595969, 'http://www.oxncloud.top/cos/public/1969752947225530369/NXMGV.png', 'logo', NULL, '', NULL, 5667, 376, 376, 1, 'png', 1969752947225530369, '2025-11-11 12:39:41', '2025-11-11 15:59:55', '2025-11-12 10:22:44', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 15:59:55', 'http://www.oxncloud.top/cos/public/1969752947225530369/NXMGV.webp', NULL, '0x080e0', 'http://www.oxncloud.top/cos/public/1969752947225530369/NXMGV.webp');
INSERT INTO `picture` VALUES (1988104622457823233, 'http://www.oxncloud.top/cos/public/1969752947225530369/jFjKD.png', 'logo', NULL, '', NULL, 5667, 376, 376, 1, 'png', 1969752947225530369, '2025-11-11 12:40:53', '2025-11-11 14:24:22', '2025-11-11 15:59:28', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 14:24:22', 'http://www.oxncloud.top/cos/public/1969752947225530369/jFjKD.webp', NULL, '0x080e0', 'http://www.oxncloud.top/cos/public/1969752947225530369/jFjKD.webp');
INSERT INTO `picture` VALUES (1988104681014501378, 'http://www.oxncloud.top/cos/public/1969752947225530369/8bPpy.png', 'logo', NULL, '电脑壁纸', '[\"风景\",\"性感\"]', 5667, 376, 376, 1, 'png', 1969752947225530369, '2025-11-11 12:41:07', '2025-11-11 14:20:45', '2025-11-11 14:21:15', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 14:20:45', 'http://www.oxncloud.top/cos/public/1969752947225530369/8bPpy.webp', NULL, '0x080e0', 'http://www.oxncloud.top/cos/public/1969752947225530369/8bPpy.webp');
INSERT INTO `picture` VALUES (1988104776300699650, 'http://www.oxncloud.top/cos/public/1969752947225530369/yn2CP.png', 'logo', NULL, '电脑壁纸', '[\"可爱\",\"风景\"]', 5667, 376, 376, 1, 'png', 1969752947225530369, '2025-11-11 12:41:30', '2025-11-11 14:09:48', '2025-11-11 15:59:26', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 14:09:48', 'http://www.oxncloud.top/cos/public/1969752947225530369/yn2CP.webp', NULL, '0x080e0', 'http://www.oxncloud.top/cos/public/1969752947225530369/yn2CP.webp');
INSERT INTO `picture` VALUES (1988105272088420354, 'http://www.oxncloud.top/cos/public/1969752947225530369/xr2xG.png', 'logo', NULL, NULL, NULL, 5667, 376, 376, 1, 'png', 1969752947225530369, '2025-11-11 12:43:28', '2025-11-11 14:06:34', '2025-11-11 15:59:23', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-11 14:06:34', 'http://www.oxncloud.top/cos/public/1969752947225530369/xr2xG.webp', NULL, '0x080e0', 'http://www.oxncloud.top/cos/public/1969752947225530369/xr2xG.webp');
INSERT INTO `picture` VALUES (1988171304358481922, 'http://www.oxncloud.top/cos/public/1976882229613694977/JDlXI.png', '薄荷曼果茶', NULL, '', NULL, 79183, 200, 200, 1, 'png', 1976882229613694977, '2025-11-11 17:05:52', '2025-11-11 17:05:54', '2025-11-11 17:05:53', 0, 0, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/JDlXI.webp', 1988171280669052929, '0x4381a0', 'http://www.oxncloud.top/cos/public/1976882229613694977/JDlXI.webp');
INSERT INTO `picture` VALUES (1988171383777628162, 'http://www.oxncloud.top/cos/public/1976882229613694977/Rewfp.png', '派蒙', NULL, '', NULL, 7781, 144, 144, 1, 'png', 1976882229613694977, '2025-11-11 17:06:10', '2025-11-11 17:06:13', '2025-11-11 17:06:12', 0, 0, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/Rewfp.webp', 1988171280669052929, '0x723639', 'http://www.oxncloud.top/cos/public/1976882229613694977/Rewfp.webp');
INSERT INTO `picture` VALUES (1988172698952003585, 'http://www.oxncloud.top/cos/public/1976882229613694977/qPoQf.png', '仙跳墙', NULL, '', NULL, 70726, 220, 220, 1, 'png', 1976882229613694977, '2025-11-11 17:11:24', '2025-11-11 17:11:26', '2025-11-11 17:11:26', 0, 1, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/qPoQf.webp', 1988171280669052929, '0x7d5d3f', 'http://www.oxncloud.top/cos/public/1976882229613694977/qPoQf.webp');
INSERT INTO `picture` VALUES (1988173340764401665, 'http://www.oxncloud.top/cos/public/1976882229613694977/WdZjE.png', '奶油蘑菇汤', NULL, '电脑壁纸', NULL, 79821, 220, 220, 1, 'png', 1976882229613694977, '2025-11-11 17:13:57', '2025-11-11 17:14:01', '2025-11-12 10:22:44', 1, 0, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/WdZjE.webp', NULL, '0x456363', 'http://www.oxncloud.top/cos/public/1976882229613694977/WdZjE.webp');
INSERT INTO `picture` VALUES (1988174234742550529, 'http://www.oxncloud.top/cos/public/1976882229613694977/Lznt2.png', '派蒙', NULL, '', NULL, 7781, 144, 144, 1, 'png', 1976882229613694977, '2025-11-11 17:17:30', '2025-11-11 17:17:32', '2025-11-12 10:22:43', 1, 0, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/Lznt2.webp', NULL, '0x723639', 'http://www.oxncloud.top/cos/public/1976882229613694977/Lznt2.webp');
INSERT INTO `picture` VALUES (1988174345962909698, 'http://www.oxncloud.top/cos/public/1976882229613694977/HtJmO.png', '奶油蘑菇汤', NULL, '', NULL, 79821, 220, 220, 1, 'png', 1976882229613694977, '2025-11-11 17:17:57', '2025-11-11 17:17:58', '2025-11-12 10:22:42', 1, 0, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/HtJmO.webp', NULL, '0x456363', 'http://www.oxncloud.top/cos/public/1976882229613694977/HtJmO.webp');
INSERT INTO `picture` VALUES (1988174392456769537, 'http://www.oxncloud.top/cos/public/1976882229613694977/6Ctzi.png', '奶油蘑菇汤', NULL, '', NULL, 79821, 220, 220, 1, 'png', 1976882229613694977, '2025-11-11 17:18:08', '2025-11-11 17:18:10', '2025-11-11 17:18:09', 0, 1, NULL, NULL, NULL, 'http://www.oxncloud.top/cos/public/1976882229613694977/6Ctzi.webp', 1988171280669052929, '0x456363', 'http://www.oxncloud.top/cos/public/1976882229613694977/6Ctzi.webp');
INSERT INTO `picture` VALUES (1988416211769741313, 'http://www.oxncloud.top/cos/public/1969752947225530369/rqltI.png', 'logo', NULL, '', '[]', 5667, 376, 376, 1, 'png', 1969752947225530369, '2025-11-12 09:19:02', '2025-11-12 10:22:04', '2025-11-12 10:22:41', 1, 1, '管理员自动过审', 1969752947225530369, '2025-11-12 10:22:04', 'http://www.oxncloud.top/cos/public/1969752947225530369/rqltI.webp', NULL, '0x080e0', 'http://www.oxncloud.top/cos/public/1969752947225530369/rqltI.webp');
INSERT INTO `picture` VALUES (1988796655954681858, 'http://www.oxncloud.top/cos/public/1969752947225530369/epXrv.jpeg', '182F7B1ECDF1D77F9B6C76C809068193', NULL, '', NULL, 179276, 1242, 700, 1.77, 'jpeg', 1969752947225530369, '2025-11-13 10:30:47', '2025-11-13 10:30:50', '2025-11-13 10:30:50', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-13 10:30:50', 'http://www.oxncloud.top/cos/public/1969752947225530369/epXrv.webp', 1988447153120968706, '0xa1c0df', 'http://www.oxncloud.top/cos/public/1969752947225530369/epXrv.webp');
INSERT INTO `picture` VALUES (1988799611240275970, 'http://www.oxncloud.top/cos/public/1969752947225530369/ukfct.jpeg', '182F7B1ECDF1D77F9B6C76C809068193', NULL, '', NULL, 130820, 340, 340, 1, 'jpeg', 1969752947225530369, '2025-11-13 10:42:32', '2025-11-13 12:19:17', '2025-11-13 12:19:16', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-13 12:19:17', 'http://www.oxncloud.top/cos/public/1969752947225530369/ukfct.webp', 1988447153120968706, '0xe0e085', 'http://www.oxncloud.top/cos/public/1969752947225530369/ukfct.webp');
INSERT INTO `picture` VALUES (1988838980353355778, 'http://www.oxncloud.top/cos/public/1969752947225530369/X4uhE.png', '【哲风壁纸】AI原神-原神', NULL, NULL, NULL, 2051388, 3600, 2400, 1.5, 'png', 1969752947225530369, '2025-11-13 13:18:58', '2025-11-13 13:18:58', '2025-11-13 13:18:58', 0, 1, '管理员自动过审', 1969752947225530369, '2025-11-13 13:18:58', 'http://www.oxncloud.top/cos/public/1969752947225530369/X4uhE_thumbnailpng', NULL, '0x000', 'http://www.oxncloud.top/cos/public/1969752947225530369/X4uhE.webp');
INSERT INTO `picture` VALUES (1996493911895420929, 'http://www.oxncloud.top/cos/public/1969752947225530369/lUI9e.png', '2025-10-17_YpGBS1OtpDXweDr1', NULL, NULL, NULL, 1784645, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-12-04 16:16:56', '2025-12-04 16:16:56', '2025-12-04 16:25:26', 1, 1, '管理员自动过审', 1969752947225530369, '2025-12-04 16:16:56', 'http://www.oxncloud.top/cos/public/1969752947225530369/lUI9e_thumbnailpng', NULL, '0x7c5d3f', 'http://www.oxncloud.top/cos/public/1969752947225530369/lUI9e.webp');
INSERT INTO `picture` VALUES (1996494314603130882, 'http://www.oxncloud.top/cos/public/1969752947225530369/Hix6D.png', '2025-10-17_YpGBS1OtpDXweDr1', NULL, NULL, NULL, 1784645, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-12-04 16:18:32', '2025-12-04 16:18:32', '2025-12-04 16:25:25', 1, 1, '管理员自动过审', 1969752947225530369, '2025-12-04 16:18:32', 'http://www.oxncloud.top/cos/public/1969752947225530369/Hix6D_thumbnailpng', NULL, '0x7c5d3f', 'http://www.oxncloud.top/cos/public/1969752947225530369/Hix6D.webp');
INSERT INTO `picture` VALUES (1996495868680859649, 'http://www.oxncloud.top/cos/public/1969752947225530369/XrSHL.jpeg', '7fafb63d-25c6-48f3-a5ab-fed20dccb44b-1', NULL, NULL, NULL, 1674250, 1664, 928, 1.79, 'jpeg', 1969752947225530369, '2025-12-04 16:24:42', '2025-12-04 16:24:42', '2025-12-04 16:25:23', 1, 1, '管理员自动过审', 1969752947225530369, '2025-12-04 16:24:43', 'http://www.oxncloud.top/cos/public/1969752947225530369/XrSHL_thumbnailjpeg', NULL, '0x6d6f57', 'http://www.oxncloud.top/cos/public/1969752947225530369/XrSHL.webp');
INSERT INTO `picture` VALUES (1996495951379951617, 'http://www.oxncloud.top/cos/public/1969752947225530369/KkMGy.png', '2025-10-17_YpGBS1OtpDXweDr1', NULL, '', NULL, 1784645, 3840, 2160, 1.78, 'png', 1969752947225530369, '2025-12-04 16:25:02', '2025-12-04 16:25:05', '2025-12-04 16:25:20', 1, 1, '管理员自动过审', 1969752947225530369, '2025-12-04 16:25:05', 'http://www.oxncloud.top/cos/public/1969752947225530369/KkMGy_thumbnailpng', NULL, '0x7c5d3f', 'http://www.oxncloud.top/cos/public/1969752947225530369/KkMGy.webp');
INSERT INTO `picture` VALUES (1996512315733721089, 'http://www.oxncloud.top/cos/public/1969752947225530369/s6cty.jpeg', '雷电将军-像素', '哈哈哈', '电脑壁纸', '[\"动漫\"]', 1675950, 1664, 928, 1.79, 'jpeg', 1969752947225530369, '2025-12-04 17:30:04', '2025-12-04 17:30:32', '2025-12-04 17:30:31', 0, 1, '管理员自动过审', 1969752947225530369, '2025-12-04 17:30:32', 'http://www.oxncloud.top/cos/public/1969752947225530369/s6cty_thumbnailjpeg', NULL, '0x808053', 'http://www.oxncloud.top/cos/public/1969752947225530369/s6cty.webp');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL COMMENT '发帖人ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '标题（可选）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '正文',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `is_top` int(11) NULL DEFAULT NULL COMMENT '是否置顶',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (14, 1969752947225530369, '算法赛1', '啊啊发放2', '2025-11-06 22:38:25', '2025-11-10 16:51:07', 0, 1);
INSERT INTO `post` VALUES (15, 1969752947225530369, '周五1', '马上放假😍2', '2025-11-07 09:23:48', '2025-11-10 20:19:19', 5, 0);
INSERT INTO `post` VALUES (19, 1969752947225530369, '第一条帖子', '哈哈哈哈哈😁', '2025-11-06 21:12:14', '2025-11-10 20:19:19', 5, 0);
INSERT INTO `post` VALUES (20, 1969752947225530369, '1', '2', '2025-12-04 16:36:47', '2025-12-04 16:36:47', 0, 0);
INSERT INTO `post` VALUES (21, 1969752947225530369, '1', '2', '2025-12-04 16:39:21', '2025-12-04 16:54:45', 0, 0);
INSERT INTO `post` VALUES (22, 1969752947225530369, '1', '2', '2025-12-04 17:02:52', '2025-12-04 17:02:52', 0, 0);
INSERT INTO `post` VALUES (23, 1969752947225530369, '1', '2', '2025-12-04 17:04:11', '2025-12-04 17:04:11', 0, 0);
INSERT INTO `post` VALUES (24, 1969752947225530369, '1', '2', '2025-12-04 17:04:33', '2025-12-04 17:04:33', 0, 0);
INSERT INTO `post` VALUES (25, 1969752947225530369, '1', '2', '2025-12-04 17:05:08', '2025-12-04 17:05:08', 0, 0);
INSERT INTO `post` VALUES (26, 1969752947225530369, '1', '2', '2025-12-04 17:13:29', '2025-12-04 17:13:29', 0, 0);

-- ----------------------------
-- Table structure for post_image
-- ----------------------------
DROP TABLE IF EXISTS `post_image`;
CREATE TABLE `post_image`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL COMMENT '所属帖子ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片URL',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `thumbnail_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `preview_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '预览图',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_image
-- ----------------------------
INSERT INTO `post_image` VALUES (18, 14, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/S4OKY.png', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/S4OKY_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (19, 14, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/mPWh8.png', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/mPWh8.webp', NULL);
INSERT INTO `post_image` VALUES (20, 14, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/sW3E9.png', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/sW3E9_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (21, 14, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/sxLi3.png', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/sxLi3_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (29, 15, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/H1sb9.png', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/H1sb9_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (30, 15, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/2y4Hn.png', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/2y4Hn_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (31, 15, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/GEBzi.jpeg', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/GEBzi_thumbnailjpeg', NULL);
INSERT INTO `post_image` VALUES (32, 15, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/L0s5Q.jpeg', 0, 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/post/L0s5Q_thumbnailjpeg', NULL);
INSERT INTO `post_image` VALUES (33, 20, 'http://www.oxncloud.top/cos/post/1BRQT.png', 0, 'http://www.oxncloud.top/cos/post/1BRQT_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (35, 21, 'http://www.oxncloud.top/cos/post/zRcMn.png', 0, 'http://www.oxncloud.top/cos/post/zRcMn_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (36, 21, 'http://www.oxncloud.top/cos/post/xl2qZ.png', 0, 'http://www.oxncloud.top/cos/post/xl2qZ_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (37, 22, 'http://www.oxncloud.top/cos/post/EgcOH.png', 0, 'http://www.oxncloud.top/cos/post/EgcOH_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (38, 23, 'http://www.oxncloud.top/cos/post/pFt45.png', 0, 'http://www.oxncloud.top/cos/post/pFt45_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (39, 24, 'http://www.oxncloud.top/cos/post/ViXpH.png', 0, 'http://www.oxncloud.top/cos/post/ViXpH_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (40, 25, 'http://www.oxncloud.top/cos/post/2NdAJ.png', 0, 'http://www.oxncloud.top/cos/post/2NdAJ_thumbnailpng', NULL);
INSERT INTO `post_image` VALUES (41, 26, 'http://www.oxncloud.top/cos/post/lGCqt.png', 0, 'http://www.oxncloud.top/cos/post/lGCqt_thumbnailpng', 'http://www.oxncloud.top/cos/post/lGCqt.webp');

-- ----------------------------
-- Table structure for space
-- ----------------------------
DROP TABLE IF EXISTS `space`;
CREATE TABLE `space`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spaceName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '空间名称',
  `spaceLevel` int(11) NULL DEFAULT 0 COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
  `maxSize` bigint(20) NULL DEFAULT 0 COMMENT '空间图片的最大总大小',
  `maxCount` bigint(20) NULL DEFAULT 0 COMMENT '空间图片的最大数量',
  `totalSize` bigint(20) NULL DEFAULT 0 COMMENT '当前空间下图片的总大小',
  `totalCount` bigint(20) NULL DEFAULT 0 COMMENT '当前空间下的图片数量',
  `userId` bigint(20) NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `spaceType` int(11) NOT NULL DEFAULT 0 COMMENT '空间类型：0-私有 1-团队',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`userId`) USING BTREE,
  INDEX `idx_spaceName`(`spaceName`) USING BTREE,
  INDEX `idx_spaceLevel`(`spaceLevel`) USING BTREE,
  INDEX `idx_spaceType`(`spaceType`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1988447153120968707 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '空间' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of space
-- ----------------------------
INSERT INTO `space` VALUES (1988447081071214594, 'mySpace', 0, 209715200, 200, 0, 0, 1969752947225530369, '2025-11-12 11:21:42', '2025-11-12 11:21:42', '2025-11-12 11:21:42', 0, 0);
INSERT INTO `space` VALUES (1988447153120968706, 'myTeam', 0, 209715200, 200, 358552, 2, 1969752947225530369, '2025-11-12 11:21:59', '2025-11-12 11:21:59', '2025-11-13 10:42:32', 0, 1);

-- ----------------------------
-- Table structure for space_user
-- ----------------------------
DROP TABLE IF EXISTS `space_user`;
CREATE TABLE `space_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spaceId` bigint(20) NOT NULL COMMENT '空间 id',
  `userId` bigint(20) NOT NULL COMMENT '用户 id',
  `spaceRole` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'viewer' COMMENT '空间角色：viewer/editor/admin',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spaceId_userId`(`spaceId`, `userId`) USING BTREE,
  INDEX `idx_spaceId`(`spaceId`) USING BTREE,
  INDEX `idx_userId`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '空间用户关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of space_user
-- ----------------------------
INSERT INTO `space_user` VALUES (21, 1988447153120968706, 1969752947225530369, 'admin', '2025-11-12 11:21:59', '2025-11-12 11:21:59');
INSERT INTO `space_user` VALUES (22, 1988447153120968706, 1976882229613694977, 'editor', '2025-11-13 11:05:18', '2025-11-13 11:05:22');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_userAccount`(`userAccount`) USING BTREE,
  INDEX `idx_userName`(`userName`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1985185395728003074 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1969752947225530369, 'test2025', '79fc7b6cd415565dd33a3bffeb5870a9', '欧晓晓', 'http://www.oxncloud.top/cos/avatar/4YHfN.webp', 'aiPicturesStore项目管理员——欧晓晓', 'admin', '2025-09-21 21:17:53', '2025-09-21 21:17:53', '2025-11-10 16:01:46', 0);
INSERT INTO `user` VALUES (1976882229613694977, 'xiaoliang', '79fc7b6cd415565dd33a3bffeb5870a9', '1', 'https://ai-pictures-store-1328310172.cos.ap-chongqing.myqcloud.com/public/1976882229613694977/3NCkz.png', '34', 'user', '2025-10-11 13:27:06', '2025-10-11 13:27:06', '2025-11-07 19:46:42', 0);
INSERT INTO `user` VALUES (1985185395728003073, 'tailiang', '79fc7b6cd415565dd33a3bffeb5870a9', '萌新', 'https://img.itouxiang.com/m12/13/9a/19cff7d61987.jpg', NULL, 'user', '2025-11-03 11:20:55', '2025-11-03 11:20:55', '2025-11-10 17:45:35', 0);

-- ----------------------------
-- Table structure for user_like
-- ----------------------------
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `target_id` bigint(20) NOT NULL COMMENT '目标ID（post_id 或 comment_id）',
  `target_type` tinyint(4) NOT NULL COMMENT '1=帖子, 2=评论',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id`, `target_id`, `target_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_like
-- ----------------------------
INSERT INTO `user_like` VALUES (20, 1969752947225530369, 15, 1, '2025-11-10 16:51:39');

SET FOREIGN_KEY_CHECKS = 1;
