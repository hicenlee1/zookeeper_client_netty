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

1. 克隆或下载本项目源码
2. 进入项目根目录
3. 执行以下Maven命令构建项目：

```bash
mvn clean package
```

构建成功后，会在`target`目录生成包含所有依赖的jar文件：`zookeeper-client-netty-1.0-SNAPSHOT-jar-with-dependencies.jar`

### 运行程序

#### 方法1：双击运行（Windows）

直接双击生成的jar文件即可运行。

#### 方法2：命令行运行

```bash
java -jar target/zookeeper-client-netty-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### 方法3：使用批处理文件（Windows）

项目提供了`run_zkclient.bat`批处理文件，双击即可运行。

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