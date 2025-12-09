#!/bin/bash

# ZooKeeper客户端启动脚本

# 检查Java是否安装
if ! command -v java &> /dev/null
then
    echo "错误: 未找到Java运行环境。请先安装Java 8或更高版本。"
    exit 1
fi

# 检查是否存在jar文件
if [ -f "zookeeper-client-netty-1.0-SNAPSHOT.jar" ]; then
    # 运行jar文件
    java -jar "zookeeper-client-netty-1.0-SNAPSHOT.jar"
else
    echo "错误: 未找到jar文件。请确保在正确的目录下运行此脚本。"
    exit 1
fi