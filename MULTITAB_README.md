# ZooKeeper 客户端多标签页功能说明

## 功能概述

基于 Redis 客户端项目的标签页（tab）实现方式，为 Zookeeper 客户端项目添加了多标签页功能，使得用户可以同时连接多个 Zookeeper 服务器。同时集成了 FlatLaf 主题支持，提供现代化的界面体验。

## 主要特性

### 1. 多标签页支持
- 每个标签页对应一个独立的 Zookeeper 会话连接
- 支持同时打开多个标签页，每个标签页可以连接不同的 Zookeeper 服务器
- 标签页之间相互独立，互不干扰

### 2. 会话管理
- 支持保存 ZooKeeper 会话配置（主机地址、端口、别名）
- 可以从已保存的会话快速创建新的连接标签页
- 支持编辑和删除已保存的会话
- 会话信息持久化保存在 `zk_sessions.json` 文件中

### 3. 连接管理
- 当用户关闭某个标签页时，自动断开该标签页对应的 Zookeeper 连接
- 当用户关闭整个 GUI 程序时，自动断开所有标签页的 Zookeeper 连接
- 确保资源的及时释放，避免连接泄漏

### 4. 标签页操作
- 每个标签页都有独立的关闭按钮（×）
- 标签页关闭按钮带有鼠标悬停效果
- 至少保留一个标签页（当关闭最后一个标签页时，会自动创建一个新的）

### 5. 主题支持
- 支持 5 种界面主题：Darcula、FlatLaf Dark、IntelliJ、FlatLaf Light、系统默认
- 默认使用 IntelliJ 亮色主题
- 支持快捷键 Ctrl+T 快速切换主题
- 主题切换即时生效，无需重启
- 自动保存用户选择的主题

## 核心类说明

### MultiTabZkClient
**位置**: `src/main/java/com/zkclient/MultiTabZkClient.java`

**功能**: 多标签页客户端主窗口
- 管理多个 ZkConnectionTab 标签页
- 提供菜单栏功能（会话管理、帮助等）
- 处理窗口关闭事件，确保所有连接正确关闭
- 集成会话管理功能

**主要方法**:
- `addNewConnectionTab()`: 添加新的连接标签页
- `addSavedSessionTab(ZkSession session)`: 从保存的会话创建标签页
- `closeAllConnections()`: 关闭所有连接
- `refreshSavedSessionsMenu()`: 刷新已保存的会话菜单

### ZkConnectionTab
**位置**: `src/main/java/com/zkclient/ZkConnectionTab.java`

**功能**: 单个连接标签页
- 封装了一个完整的 ZooKeeper 连接界面
- 包含连接面板、树形结构、数据展示等组件
- 管理独立的 ZkClient 实例和线程池
- 实现按需加载节点树

**主要方法**:
- `connectToZooKeeper()`: 连接到 ZooKeeper 服务器
- `disconnectFromZooKeeper()`: 断开连接
- `closeTab()`: 关闭标签页（断开连接并通知父窗口）
- `disconnectFromZkOnly()`: 仅断开连接（用于批量关闭）

### ZkSession
**位置**: `src/main/java/com/zkclient/ZkSession.java`

**功能**: ZooKeeper 会话配置类
- 存储连接信息（主机、端口、别名）
- 支持 JSON 序列化和反序列化
- 提供连接字符串拼接功能

### ThemeManager
**位置**: `src/main/java/com/zkclient/ThemeManager.java`

**功能**: 主题管理器（单例模式）
- 管理应用程序的主题切换和管理
- 提供 5 种主题选择
- 支持主题持久化配置
- 实现主题即时切换功能

## 使用说明

### 启动程序
```bash
# 编译项目
mvn clean package -DskipTests

# 运行程序
java -jar target/zookeeper-client-netty-1.0-SNAPSHOT-jar-with-dependencies.jar
```

或者直接运行主类：
```bash
java -cp target/zookeeper-client-netty-1.0-SNAPSHOT-jar-with-dependencies.jar com.zkclient.MainApp
```

### 基本操作

#### 1. 新建连接标签页
- 菜单栏 → 会话 → 新建连接标签页
- 或者关闭所有标签页后自动创建

#### 2. 保存会话
- 菜单栏 → 会话 → 添加新会话
- 输入会话别名、主机地址、端口

#### 3. 使用已保存的会话
- 菜单栏 → 会话 → 已保存的会话 → 选择会话
- 或者 菜单栏 → 会话 → 管理会话 → 选择会话 → 连接

#### 4. 切换主题
- 菜单栏 → 主题 → 主题设置...
- 或者使用快捷键 Ctrl+T
- 选择想要的主题并确认

#### 5. 管理会话
- 菜单栏 → 会话 → 管理会话
- 可以编辑、删除已保存的会话

#### 5. 关闭标签页
- 点击标签页上的 × 按钮
- 或者 菜单栏 → 会话 → 关闭当前标签页

#### 6. 退出程序
- 菜单栏 → 会话 → 退出
- 或者直接关闭窗口
- 程序会自动断开所有连接并保存会话信息

## 技术特点

### 1. 资源管理
- 每个标签页都有独立的线程池（ExecutorService）
- 标签页关闭时自动释放线程池和 ZooKeeper 连接
- 程序退出时批量关闭所有连接，确保资源释放

### 2. 线程安全
- 使用 SwingUtilities.invokeLater 确保 UI 操作在 EDT 线程执行
- 后台线程处理 ZooKeeper 连接操作，避免 UI 阻塞
- 会话管理器使用 CopyOnWriteArrayList 保证线程安全

### 3. 异步加载
- 树形节点按需加载，避免一次性加载所有节点
- 使用 TreeWillExpandListener 实现懒加载
- 显示加载中提示，提升用户体验

### 4. 错误处理
- 连接超时处理（30秒）
- 连接失败提示
- 会话过期自动重连机制

## 文件结构
```
src/main/java/com/zkclient/
├── MainApp.java                 # 主入口（启动多标签页版本）
├── MultiTabZkClient.java       # 多标签页主窗口
├── ZkConnectionTab.java        # 单个连接标签页
├── ZkSession.java              # 会话配置类
├── ZkSessionManager.java       # 会话管理器
├── ThemeManager.java           # 主题管理器
├── ZkClient.java               # ZooKeeper 客户端封装
├── ZkTreeModel.java            # 树形模型
├── ZkNode.java                 # 节点数据类
└── ZkClientGUI.java            # 原单窗口版本（保留）
```

## 配置文件
- `zk_sessions.json`: 保存的 ZooKeeper 会话配置

## 兼容性
- 保留了原有的 `ZkClientGUI` 单窗口版本
- 主入口 `MainApp` 默认启动多标签页版本
- 可以通过修改 `MainApp.java` 切换回单窗口版本

## 注意事项
1. 确保关闭标签页或退出程序时等待连接正常断开
2. 会话配置文件 `zk_sessions.json` 会自动创建和更新
3. 每个标签页都会消耗系统资源，建议根据需要打开适量的标签页
4. 如果 ZooKeeper 服务器不可用，连接会超时（30秒）

## 后续优化建议
1. 添加标签页重命名功能
2. 支持拖拽排序标签页
3. 添加标签页图标（显示连接状态）
4. 支持标签页快捷键切换（Ctrl+Tab）
5. 添加连接状态指示器
6. 支持批量操作（批量关闭、批量刷新等）
