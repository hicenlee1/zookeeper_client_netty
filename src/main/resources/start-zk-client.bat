::代码页更改为Unicode(UTF-8)
chcp 65001
@echo off
REM ZooKeeper客户端启动脚本

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java运行环境。请先安装Java 8或更高版本。
    pause
    exit /b 1
)

REM 检查是否存在jar文件
if exist "zookeeper-client-netty-1.0-SNAPSHOT.jar" (
    REM 运行jar文件
    java -jar "zookeeper-client-netty-1.0-SNAPSHOT.jar"
) else (
    echo 错误: 未找到jar文件。请确保在正确的目录下运行此脚本。
    pause
    exit /b 1
)

REM 如果用户关闭程序，等待用户按任意键退出
if %errorlevel% neq 0 (
    echo 程序执行出错，错误代码: %errorlevel%
    pause
)