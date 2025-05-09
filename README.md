# 你画我猜

## 基于Spring+Vue开发的你画我猜Web小游戏

本项目是一款基于Web的实时互动绘画游戏平台，采用前后端分离架构开发。通过Spring Boot构建高性能后端服务，结合Vue3实现响应式前端界面，使用Socket.IO实现实时通信。支持多房间游戏、实时画板同步、聊天互动、等功能。

在线体验——公共房间[你画我猜](https://www.guetzjb.cn/draw/#/index)

自定义房间——[你画我猜](https://www.guetzjb.cn/draw/#/index?roomName=test)

https://www.guetzjb.cn/draw/#/index?roomName=test
只需加上参数roomName，后面是房间名称，支持多房间游玩、以及移动端，PC端共同游玩。

### 核心功能模块

#### 1. 游戏大厅系统

- 基于query的自定义房间创建
- 用户身份验证（JWT）

#### 2. 实时系统

- 基于canvas的绘画引擎
- 实时绘画笔迹同步（坐标/颜色/粗细）
- 画布快照保存（PNG格式、保存于OSS对象存储）
- 实时房间聊天

#### 3. 游戏逻辑系统

- 智能词语库管理，可以自行增加绘画主题
- 自定义每轮画画时长，所有人答对智能降低剩余等待时长
- 自定义每个房间座位数，想多少人就多少人！
- 回合制游戏流程控制
- 即时得分计算
- 作答检测机制

#### 4. 实时通信系统

- WebSocket长连接管理
- 消息类型协议设计：
  - 绘画数据协议
  - 聊天消息协议
  - 游戏状态协议
  - 系统通知协议

draw-guess-java为后端代码、draw-guess-front为前端代码

前端启动步骤

1、`pnpm i`

2、`pnpm dev`

后端启动步骤

1、使用sql文件创建基础数据

2、配置好redis以及mysql

3、启动

![等待节目](C:\Users\rog\Pictures\等待节目.png)

![QQ20250423-174836](C:\Users\rog\Pictures\QQ20250423-174836.png)

![QQ20250423-174803](C:\Users\rog\Pictures\QQ20250423-174803.png)

![QQ20250423-174909](C:\Users\rog\Pictures\QQ20250423-174909.png)