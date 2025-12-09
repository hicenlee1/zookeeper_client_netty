# ZooKeeper客户端 - Netty版本

一个基于Java Swing和Netty 4开发的ZooKeeper图形化客户端工具。

## 功能特点

- **树形查看ZooKeeper节点**：直观展示ZooKeeper的节点层次结构
- **查看节点内容**：显示选中节点的数据内容
- **查看节点属性**：显示节点的详细属性信息
- **连接管理**：支持连接到任意ZooKeeper服务器
- **异步操作**：采用线程池处理ZooKeeper操作，避免界面卡顿
- **美观界面**：使用Swing原生组件，支持系统外观

## 技术栈

- Java 8+
- Netty 4.1.68.Final
- Apache ZooKeeper 3.6.3
- Swing (图形界面)
- Logback (日志)
- Maven (项目管理)

## 安装与使用

### 前提条件

- 已安装Java 8或更高版本
- 已安装Maven（用于构建项目）
- 可访问的ZooKeeper服务器

### 构建项目

本项目支持两种打包方式：

1. **基础打包（默认）**：执行基础打包流程，生成的JAR文件位于`target`目录
   ```bash
   mvn clean package
   ```

2. **完整打包（推荐）**：执行完整打包流程，生成包含所有依赖和启动脚本的`dist`目录
   ```bash
   mvn clean package -Penv
   ```

### 运行程序

#### 使用完整打包（推荐）

如果使用完整打包方式（-Penv），构建成功后会在项目根目录生成`dist`目录，其中包含：

- `zookeeper-client-netty-1.0-SNAPSHOT.jar`：主程序JAR文件
- `lib`目录：所有依赖的JAR文件
- `start-zk-client.bat`：Windows启动脚本
- `start-zk-client.sh`：Linux/Mac启动脚本
- `logs`目录：程序运行时的日志文件

可以通过以下方式运行：

##### 方法1：使用启动脚本（推荐）

- Windows系统：双击`dist`目录下的`start-zk-client.bat`文件
- Linux/Mac系统：在终端中执行`dist`目录下的`start-zk-client.sh`脚本

##### 方法2：命令行运行

```bash
java -jar dist/zookeeper-client-netty-1.0-SNAPSHOT.jar
```

#### 使用基础打包

如果使用基础打包方式，构建成功后会在`target`目录生成JAR文件：

```bash
java -jar target/zookeeper-client-netty-1.0-SNAPSHOT.jar
```

## 使用说明

1. 在界面顶部输入ZooKeeper服务器的主机名（默认为localhost）和端口号（默认为2181）
2. 点击"连接"按钮连接到ZooKeeper服务器
3. 连接成功后，左侧树状图会显示ZooKeeper的节点结构
4. 点击任意节点，右侧会显示该节点的数据内容和属性信息
5. 底部状态栏会显示当前连接状态

## 注意事项

- 确保ZooKeeper服务器可访问且端口正确
- 长时间不操作可能导致会话过期，程序会自动尝试重连
- 程序日志保存在`logs`目录下

## Maven配置说明

本项目使用Maven Profiles来管理不同的构建配置：

### 默认Profile（default）

- **激活方式**：默认激活，无需指定参数
- **功能**：执行基础打包流程
- **输出位置**：生成的JAR文件位于`target`目录
- **依赖处理**：依赖不会单独分离，需要在运行时确保类路径正确

### env Profile（完整打包）

- **激活方式**：使用`-Penv`参数激活
- **功能**：执行完整的分发打包流程，包括：
  1. 将主JAR文件输出到`dist`目录
  2. 将所有依赖复制到`dist/lib`目录
  3. 将启动脚本复制到`dist`目录
  4. 设置shell脚本的可执行权限
  5. 创建`dist/logs`目录用于存放日志文件
- **优势**：
  - 分离主程序和依赖，便于维护和升级
  - 提供跨平台的启动脚本
  - 包含完整的运行时环境

### 灵活的打包方式选择

通过使用不同的Maven Profile，开发者可以根据需要选择合适的打包方式：
- 开发阶段可使用默认Profile快速构建和测试
- 发布版本建议使用env Profile，提供完整的分发包

## 开发说明

如果您想参与开发或修改此项目，可以使用任意Java IDE（如IntelliJ IDEA、Eclipse等）导入项目。

主要类说明：
- `MainApp`：程序入口类
- `ZkClientGUI`：图形界面主类
- `ZkClient`：ZooKeeper客户端核心类，封装了ZooKeeper操作
- `ZkTreeModel`：树模型类，用于JTree组件
- `ZkNode`：ZooKeeper节点数据模型类

## 许可证

本项目采用MIT许可证。