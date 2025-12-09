package com.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ZooKeeper客户端核心类，封装了与ZooKeeper服务器的交互操作
 */
public class ZkClient {
    private static final Logger logger = LoggerFactory.getLogger(ZkClient.class);
    private ZooKeeper zk;
    private CountDownLatch connectedLatch = new CountDownLatch(1);
    private String connectString;
    private int sessionTimeout = 30000; // 默认会话超时时间30秒

    /**
     * 连接ZooKeeper服务器
     * @param connectString ZooKeeper服务器连接字符串，格式为host:port
     * @return 是否连接成功
     */
    public boolean connect(String connectString) {
        this.connectString = connectString;
        try {
            zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        connectedLatch.countDown();
                        logger.info("成功连接到ZooKeeper服务器");
                    } else if (event.getState() == Event.KeeperState.Disconnected) {
                        connectedLatch = new CountDownLatch(1);
                        logger.warn("与ZooKeeper服务器断开连接");
                    } else if (event.getState() == Event.KeeperState.Closed) {
                        connectedLatch = new CountDownLatch(1);
                        logger.warn("与ZooKeeper服务器关闭连接");
                    } else if (event.getState() == Event.KeeperState.Expired) {
                        logger.error("会话过期，尝试重新连接");
                        try {
                            reconnect();
                        } catch (Exception e) {
                            logger.error("重新连接失败", e);
                        }
                    } else if (event.getState() == Event.KeeperState.AuthFailed) {
                        logger.error("认证失败");
                        connectedLatch.countDown();
                    } else if (event.getState() == Event.KeeperState.ConnectedReadOnly) {
                        logger.warn("以只读方式连接");
                        connectedLatch.countDown();
                    }
                }
            });
            // 添加超时时间，防止一直等待
            boolean connected = connectedLatch.await(30, TimeUnit.SECONDS);
            if (!connected) {
                logger.error("连接ZooKeeper服务器超时");
                // 超时后关闭连接
                if (zk != null) {
                    try {
                        zk.close();
                    } catch (InterruptedException e) {
                        logger.error("关闭超时连接失败", e);
                    }
                }
                zk = null;
            }
            return connected;
        } catch (IOException e) {
            logger.error("创建ZooKeeper连接失败", e);
            return false;
        } catch (InterruptedException e) {
            logger.error("连接过程被中断", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 重新连接ZooKeeper服务器
     */
    private void reconnect() throws IOException, InterruptedException {
        connectedLatch = new CountDownLatch(1);
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    connectedLatch.countDown();
                    logger.info("重新连接到ZooKeeper服务器成功");
                }
            }
        });
        connectedLatch.await();
    }

    /**
     * 获取节点子列表
     * @param path 节点路径
     * @return 子节点列表
     */
    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        if (zk == null || !zk.getState().isConnected()) {
            throw new KeeperException.ConnectionLossException();
        }
        return zk.getChildren(path, false);
    }

    /**
     * 获取节点数据
     * @param path 节点路径
     * @param stat 节点状态
     * @return 节点数据
     */
    public byte[] getData(String path, Stat stat) throws KeeperException, InterruptedException {
        if (zk == null || !zk.getState().isConnected()) {
            throw new KeeperException.ConnectionLossException();
        }
        return zk.getData(path, false, stat);
    }

    /**
     * 获取节点状态
     * @param path 节点路径
     * @return 节点状态
     */
    public Stat getStat(String path) throws KeeperException, InterruptedException {
        if (zk == null || !zk.getState().isConnected()) {
            throw new KeeperException.ConnectionLossException();
        }
        return zk.exists(path, false);
    }

    /**
     * 创建节点
     * @param path 节点路径
     * @param data 节点数据
     * @param acl 访问控制列表
     * @param createMode 创建模式
     * @return 创建的节点路径
     */
    public String createNode(String path, byte[] data, List<ACL> acl, CreateMode createMode) throws KeeperException, InterruptedException {
        if (zk == null || !zk.getState().isConnected()) {
            throw new KeeperException.ConnectionLossException();
        }
        return zk.create(path, data, acl, createMode);
    }

    /**
     * 更新节点数据
     * @param path 节点路径
     * @param data 新的节点数据
     * @param version 版本号，如果为-1则忽略版本检查
     * @return 更新后的节点状态
     */
    public Stat updateNode(String path, byte[] data, int version) throws KeeperException, InterruptedException {
        if (zk == null || !zk.getState().isConnected()) {
            throw new KeeperException.ConnectionLossException();
        }
        return zk.setData(path, data, version);
    }

    /**
     * 删除节点
     * @param path 节点路径
     * @param version 版本号，如果为-1则忽略版本检查
     */
    public void deleteNode(String path, int version) throws KeeperException, InterruptedException {
        if (zk == null || !zk.getState().isConnected()) {
            throw new KeeperException.ConnectionLossException();
        }
        zk.delete(path, version);
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (zk != null) {
            try {
                zk.close();
                logger.info("已关闭ZooKeeper连接");
            } catch (InterruptedException e) {
                logger.error("关闭ZooKeeper连接时发生异常", e);
            }
        }
    }

    /**
     * 检查是否已连接
     * @return 是否已连接
     */
    public boolean isConnected() {
        return zk != null && zk.getState().isConnected();
    }
}