package com.zkclient;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用程序主入口类
 * 已迁移至多标签页版本（MultiTabZkClient）
 */
public class MainApp {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        logger.info("ZooKeeper客户端启动（多标签页版本）");
        
        // 初始化并应用主题
        ThemeManager.initTheme();
        
        // 启动多标签页版本的客户端
        MultiTabZkClient.main(args);
    }
}