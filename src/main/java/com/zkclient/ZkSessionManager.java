package com.zkclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 会话管理器，负责ZooKeeper会话的存储、加载和管理
 */
public class ZkSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(ZkSessionManager.class);
    private static final String SESSION_FILE = "zk_sessions.json";
    private static ZkSessionManager instance;
    private List<ZkSession> sessions;
    private ObjectMapper objectMapper;
    
    private ZkSessionManager() {
        sessions = new CopyOnWriteArrayList<>();
        objectMapper = new ObjectMapper();
        // 配置Jackson以美化输出
        // writerWithDefaultPrettyPrinter()方法会在saveSessions方法中直接调用
        loadSessions();
    }
    
    public static synchronized ZkSessionManager getInstance() {
        if (instance == null) {
            instance = new ZkSessionManager();
        }
        return instance;
    }
    
    /**
     * 加载保存的会话列表
     */
    private void loadSessions() {
        File file = new File(SESSION_FILE);
        
        if (!file.exists()) {
            // 如果文件不存在，添加默认会话
            addDefaultSessions();
            return;
        }
        
        try {
            // 使用Jackson读取JSON文件
            sessions = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, ZkSession.class));
            logger.info("成功加载 {} 个会话", sessions.size());
        } catch (Exception e) {
            logger.error("加载会话失败", e);
            // 加载失败时添加默认会话
            addDefaultSessions();
        }
    }
    
    /**
     * 添加默认会话
     */
    private void addDefaultSessions() {
        sessions.clear();
        addSession(new ZkSession("本地ZooKeeper", "localhost", "2181"));
    }
    
    /**
     * 保存会话列表
     */
    public void saveSessions() {
        try {
            // 使用Jackson保存为JSON格式
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(SESSION_FILE), new ArrayList<>(sessions));
            logger.info("成功保存 {} 个会话", sessions.size());
        } catch (Exception e) {
            logger.error("保存会话失败", e);
        }
    }
    
    /**
     * 添加新会话
     */
    public void addSession(ZkSession session) {
        if (!sessions.contains(session)) {
            sessions.add(session);
            saveSessions();
        }
    }
    
    /**
     * 删除会话
     */
    public void removeSession(ZkSession session) {
        if (sessions.remove(session)) {
            saveSessions();
        }
    }
    
    /**
     * 获取所有会话
     */
    public List<ZkSession> getAllSessions() {
        return new ArrayList<>(sessions);
    }
    
    /**
     * 更新会话
     */
    public void updateSession(ZkSession oldSession, ZkSession newSession) {
        removeSession(oldSession);
        addSession(newSession);
    }
}