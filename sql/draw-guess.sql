/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : draw-guess

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 12/05/2025 15:25:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for draw
-- ----------------------------
DROP TABLE IF EXISTS `draw`;
CREATE TABLE `draw`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of draw
-- ----------------------------

-- ----------------------------
-- Table structure for draw_title
-- ----------------------------
DROP TABLE IF EXISTS `draw_title`;
CREATE TABLE `draw_title`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of draw_title
-- ----------------------------
INSERT INTO `draw_title` VALUES (11, '自行车');
INSERT INTO `draw_title` VALUES (12, '雨伞');
INSERT INTO `draw_title` VALUES (13, '闹钟');
INSERT INTO `draw_title` VALUES (14, '冰箱');
INSERT INTO `draw_title` VALUES (15, '书包');
INSERT INTO `draw_title` VALUES (16, '钥匙');
INSERT INTO `draw_title` VALUES (17, '电梯');
INSERT INTO `draw_title` VALUES (18, '枕头');
INSERT INTO `draw_title` VALUES (19, '洗衣机');
INSERT INTO `draw_title` VALUES (20, '望远镜');
INSERT INTO `draw_title` VALUES (21, '彩虹');
INSERT INTO `draw_title` VALUES (22, '火山');
INSERT INTO `draw_title` VALUES (23, '蒲公英');
INSERT INTO `draw_title` VALUES (24, '瀑布');
INSERT INTO `draw_title` VALUES (25, '闪电');
INSERT INTO `draw_title` VALUES (26, '蜗牛');
INSERT INTO `draw_title` VALUES (27, '企鹅');
INSERT INTO `draw_title` VALUES (28, '章鱼');
INSERT INTO `draw_title` VALUES (29, '啄木鸟');
INSERT INTO `draw_title` VALUES (30, '萤火虫');
INSERT INTO `draw_title` VALUES (31, '医生');
INSERT INTO `draw_title` VALUES (32, '厨师');
INSERT INTO `draw_title` VALUES (33, '消防员');
INSERT INTO `draw_title` VALUES (34, '宇航员');
INSERT INTO `draw_title` VALUES (35, '魔术师');
INSERT INTO `draw_title` VALUES (36, '海盗');
INSERT INTO `draw_title` VALUES (37, '农民');
INSERT INTO `draw_title` VALUES (38, '侦探');
INSERT INTO `draw_title` VALUES (39, '摄影师');
INSERT INTO `draw_title` VALUES (40, '快递员');
INSERT INTO `draw_title` VALUES (41, '跳绳');
INSERT INTO `draw_title` VALUES (42, '钓鱼');
INSERT INTO `draw_title` VALUES (43, '踢足球');
INSERT INTO `draw_title` VALUES (44, '放风筝');
INSERT INTO `draw_title` VALUES (45, '打喷嚏');
INSERT INTO `draw_title` VALUES (46, '吃火锅');
INSERT INTO `draw_title` VALUES (47, '爬山');
INSERT INTO `draw_title` VALUES (48, '弹吉他');
INSERT INTO `draw_title` VALUES (49, '拍照');
INSERT INTO `draw_title` VALUES (50, '写作业');
INSERT INTO `draw_title` VALUES (51, '冰淇淋');
INSERT INTO `draw_title` VALUES (52, '汉堡包');
INSERT INTO `draw_title` VALUES (53, '西瓜');
INSERT INTO `draw_title` VALUES (54, '火锅');
INSERT INTO `draw_title` VALUES (55, '蛋糕');
INSERT INTO `draw_title` VALUES (56, '奶茶');
INSERT INTO `draw_title` VALUES (57, '披萨');
INSERT INTO `draw_title` VALUES (58, '糖葫芦');
INSERT INTO `draw_title` VALUES (59, '爆米花');
INSERT INTO `draw_title` VALUES (60, '烤红薯');

-- ----------------------------
-- Table structure for game_round
-- ----------------------------
DROP TABLE IF EXISTS `game_round`;
CREATE TABLE `game_round`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `round` int NULL DEFAULT NULL,
  `seat` int NULL DEFAULT NULL,
  `start_game_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `draw_title_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK8l03hob2ejekjg6knxj1jgmok`(`start_game_id` ASC) USING BTREE,
  INDEX `FKmb66jyt3tke68jfd1jsqfnb98`(`user_id` ASC) USING BTREE,
  INDEX `FK7v0vuintb7k0nu1h6mnus25j6`(`draw_title_id` ASC) USING BTREE,
  CONSTRAINT `FK7v0vuintb7k0nu1h6mnus25j6` FOREIGN KEY (`draw_title_id`) REFERENCES `draw_title` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK8l03hob2ejekjg6knxj1jgmok` FOREIGN KEY (`start_game_id`) REFERENCES `start_game` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKmb66jyt3tke68jfd1jsqfnb98` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_round
-- ----------------------------

-- ----------------------------
-- Table structure for start_game
-- ----------------------------
DROP TABLE IF EXISTS `start_game`;
CREATE TABLE `start_game`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `crete_time` datetime(6) NULL DEFAULT NULL,
  `joined_count` int NULL DEFAULT NULL COMMENT '房间总人数',
  `room_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK91dnjphewc6xk9j6spgpjdnxd`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK91dnjphewc6xk9j6spgpjdnxd` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of start_game
-- ----------------------------
INSERT INTO `start_game` VALUES (1, '2025-04-13 14:41:41.984000', 1, 'public', 1);
INSERT INTO `start_game` VALUES (2, '2025-04-13 14:45:08.612000', 1, 'public', 3);
INSERT INTO `start_game` VALUES (3, '2025-04-13 14:47:02.534000', 1, 'public', 4);
INSERT INTO `start_game` VALUES (4, '2025-04-13 14:50:38.816000', 1, 'public', 5);
INSERT INTO `start_game` VALUES (5, '2025-04-13 15:21:34.887000', 1, 'public', 6);
INSERT INTO `start_game` VALUES (6, '2025-04-13 15:23:00.989000', 1, 'public', 7);
INSERT INTO `start_game` VALUES (7, '2025-04-13 15:25:06.823000', 1, 'public', 8);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'https://robohash.org/b9PD4PlT', '2025-04-13 14:41:13.280000', 'Owen Big', '123456', 'b9PD4PlT');
INSERT INTO `user` VALUES (2, 'https://robohash.org/XWqxjEqq', '2025-04-13 14:42:59.403000', 'Jack Hammer', '123456', 'XWqxjEqq');
INSERT INTO `user` VALUES (3, 'https://robohash.org/oPZzvXaU', '2025-04-13 14:45:07.167000', 'Cal Efornia', '123456', 'oPZzvXaU');
INSERT INTO `user` VALUES (4, 'https://robohash.org/DPG51Fo7', '2025-04-13 14:47:01.190000', 'Cammie Sole', '123456', 'DPG51Fo7');
INSERT INTO `user` VALUES (5, 'https://robohash.org/OWUA4M7C', '2025-04-13 14:50:37.613000', 'Crystal Glass', '123456', 'OWUA4M7C');
INSERT INTO `user` VALUES (6, 'https://robohash.org/2eG_LSuj', '2025-04-13 15:21:33.384000', 'Sir Fin Waves', '123456', '2eG_LSuj');
INSERT INTO `user` VALUES (7, 'https://robohash.org/JwALpbjb', '2025-04-13 15:22:59.458000', 'Ted E. Baer', '123456', 'JwALpbjb');
INSERT INTO `user` VALUES (8, 'https://robohash.org/JiY_GzDx', '2025-04-13 15:25:05.412000', 'Mason Jarr', '123456', 'JiY_GzDx');
INSERT INTO `user` VALUES (9, 'https://robohash.org/ByF59sjz', '2025-04-13 15:38:41.719000', 'Trina Corder', '123456', 'ByF59sjz');
INSERT INTO `user` VALUES (10, 'https://robohash.org/6dS1GWar', '2025-05-12 10:13:02.903000', 'Mike Stand', '123456', '6dS1GWar');
INSERT INTO `user` VALUES (11, 'https://robohash.org/e33yYRhH', '2025-05-12 10:28:45.469000', 'Sal A. Mander', '123456', 'e33yYRhH');
INSERT INTO `user` VALUES (12, 'https://robohash.org/FE1W5Cbe', '2025-05-12 14:52:55.037000', 'Polly Dent', '123456', 'FE1W5Cbe');
INSERT INTO `user` VALUES (13, 'https://robohash.org/d3jFrehT', '2025-05-12 14:53:26.484000', 'Al Fresco', '123456', 'd3jFrehT');
INSERT INTO `user` VALUES (14, 'https://robohash.org/YPVzWyMa', '2025-05-12 14:54:29.378000', 'Mary Gold', '123456', 'YPVzWyMa');

SET FOREIGN_KEY_CHECKS = 1;
