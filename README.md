# 你画我猜

## 基于Spring+Vue开发的你画我猜Web小游戏

> 项目地址 https://github.com/ifavcode/nihuawocai

本项目是一款基于Web的实时互动绘画游戏平台，采用前后端分离架构开发。通过Spring Boot构建高性能后端服务，结合Vue3实现响应式前端界面，使用Socket.IO实现实时通信。支持多房间游戏、实时画板同步、聊天互动、等功能。

在线体验——公共房间[你画我猜](https://www.guetzjb.cn/draw/#/index)

自定义房间——[你画我猜](https://www.guetzjb.cn/draw/#/index?roomName=test)

https://www.guetzjb.cn/draw/#/index?roomName=test
只需加上参数roomName，后面是房间名称，支持多房间游玩、以及移动端，PC端共同游玩。

#### 核心功能模块

##### 1. 游戏大厅系统

- 基于query的自定义房间创建
- 用户身份验证（JWT）

##### 2. 实时系统

- 基于canvas的绘画引擎
- 实时绘画笔迹同步（坐标/颜色/粗细）
- 画布快照保存（PNG格式、保存于OSS对象存储）
- 实时房间聊天

##### 3. 游戏逻辑系统

- 智能词语库管理，可以自行增加绘画主题
- 自定义每轮画画时长，所有人答对智能降低剩余等待时长
- 自定义每个房间座位数，想多少人就多少人！
- 回合制游戏流程控制
- 即时得分计算
- 作答检测机制

##### 4. 实时通信系统

- WebSocket长连接管理
- 消息类型协议设计：
  - 绘画数据协议
  - 聊天消息协议
  - 游戏状态协议
  - 系统通知协议

```plaintext
nihuawocai/
├── draw-guess-java  // 后端代码
├── draw-guess-front // 前端代码
```

#### 细节

当用户进入项目地址，如http://localhost:5137，没有填写具体的房间号，系统会默认进入public房间号，相同浏览器只算第一次进入的标签页，测试需要使用两个不同的浏览器。

![QQ20250509-154856.png (1192×391)](https://ifavcode.github.io/images/QQ20250509-154856.png)

![QQ20250509-155000.png (882×408)](https://ifavcode.github.io/images/QQ20250509-155000.png)

第一个座位的玩家有权限开始游戏，系统会默认给出一个画画主题（仅你可见），换座位实时通知房间内所有玩家。

![QQ20250509-160725.png (1752×584)](https://ifavcode.github.io/images/QQ20250509-160725.png)

其他操作下载项目体验，实现细节可以查看具体代码。

#### 启动

##### **前端启动步骤**

1、安装node.js

2、`npm install -g pnpm`

3、`pnpm i`

4、`pnpm dev`

##### **后端启动步骤**

1、安装并配置好mysql,redis,maven,java21(必选)和navicat(可选)

- 配置MYSQL数据库

```sql
mysql -u username -p

# 使用cmd或者使用navicat，以下是cmd示例
mysql> CREATE DATABASE draw-guess
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;
mysql> USE draw-guess;
mysql> SOURCE draw-guess.sql;# 你的sql路径

# 修改项目中mysql的账号密码
# draw-guess-java/src/main/resources/application-dev.yml
```

- 配置redis

```sh
# 修改项目中redis的地址、密码
# draw-guess-java/src/main/resources/application-dev.yml
```

- oss配置

```yaml
# application.yml
oss:
  region: cn-shanghai
  endpoint: https://oss-cn-shanghai.aliyuncs.com
  domain: https://oss.guetzjb.cn/
  bucket: oss-pai-hdzvw7bjvj2tv17tu8-cn-shanghai
```

设置oss环境变量**（若不保存可跳过）**

```sh
set OSS_ACCESS_KEY_ID=YOUR_ACCESS_KEY
set OSS_ACCESS_KEY_SECRET=YOUR_SECRET
```

application-dev.yml示例配置如下

```yml
draw:
  max-person: 6 # 房间内最大人数
  seconds: 60 # 每盘游戏画画时间
  ns: /draw-socket # websocket转发url
  db: # mysql
    url: jdbc:mysql://127.0.0.1:3306/draw-guess?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  redis: # redis
    host: 127.0.0.1
    password: 123456
```

**每一轮画画结束，系统会自动保存结果到oss存储中**

2、在后端代码根目录运行`mvn clean install`，等待构筑完成

3、找到构筑完成的jar,在cmd输入`java -jar [构筑完的jar文件名字].jar`

现在，你可以启动后端项目，并且访问你的页面http://localhost:5173

#### Docker启动

```sh
# 克隆项目
git clone https://github.com/ifavcode/nihuawocai.git
# 补充oss环境变量（docker-compose.yml）可选
- OSS_ACCESS_KEY_ID=YOUR_ACCESS_KEY
- OSS_ACCESS_KEY_SECRET=YOUR_SECRET
# 启动
docker compose up -d
# 你可以查看应用启动情况
docker compose logs -f
# 打开http://IP:5173
```

使用到的镜像

> maven:3.9-eclipse-temurin-21、eclipse-temurin:21-jre、node:20、nginx:alpine、mysql:8.0、redis:alpine

#### 项目浏览

![QQ20250423-174803](https://ifavcode.github.io/images/QQ20250423-174803.png)

![QQ20250423-174836](https://ifavcode.github.io/images/QQ20250423-174836.png)
![QQ20250423-174909](https://ifavcode.github.io/images/QQ20250423-174909.png)
![等待节目](https://ifavcode.github.io/images/等待节目.png)

