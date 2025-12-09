package com.zkclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 支持多标签页的ZooKeeper客户端主类
 */
public class MultiTabZkClient extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MultiTabZkClient.class);
    
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private JMenu sessionMenu;
    private JMenuItem newTabMenuItem;
    private JMenuItem closeTabMenuItem;
    private JMenuItem exitMenuItem;
    private JMenu helpMenu;
    private JMenuItem aboutMenuItem;
    private JMenuItem addSessionMenuItem;
    private JMenuItem manageSessionsMenuItem;
    private JMenu savedSessionsMenu;
    private JMenu themeMenu;
    private JMenuItem themeSettingsMenuItem;
    
    /**
     * 主方法，程序入口
     */
    public static void main(String[] args) {
        // 在Event Dispatch Thread中启动UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 初始化并应用主题
                    ThemeManager.initTheme();
                    
                    // 创建并显示主窗口
                    MultiTabZkClient client = new MultiTabZkClient();
                    client.setVisible(true);
                    
                    logger.info("ZooKeeper客户端启动成功");
                } catch (Exception e) {
                    logger.error("ZooKeeper客户端启动失败: {}", e.getMessage());
                    JOptionPane.showMessageDialog(null, 
                            "客户端启动失败: " + e.getMessage(), 
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    /**
     * 构造函数，初始化主窗口和组件
     */
    public MultiTabZkClient() {
        super("ZooKeeper客户端 (多标签版)");
        
        // 设置窗口属性
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // 居中显示
        
        // 设置窗口图标
        setWindowIcon();
        
        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
        
        // 初始化组件
        initComponents();
        setupMenuBar();
        setupListeners();
        
        // 添加初始标签页
        addNewConnectionTab();
    }
    
    /**
     * 设置窗口图标
     */
    private void setWindowIcon() {
        try {
            // 使用IconManager获取应用程序图标
            ImageIcon icon = IconManager.getInstance().getApplicationIcon();
            setIconImage(icon.getImage());
        } catch (Exception e) {
            logger.warn("设置窗口图标失败: {}", e.getMessage());
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initComponents() {
        // 创建标签面板
        tabbedPane = new JTabbedPane();
        
        // 将标签面板添加到主面板
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        // 设置窗口最小大小
        setMinimumSize(new Dimension(800, 600));
    }
    
    /**
     * 设置菜单栏
     */
    private void setupMenuBar() {
        // 创建菜单栏
        menuBar = new JMenuBar();
        
        // 会话菜单
        sessionMenu = new JMenu("会话");
        newTabMenuItem = new JMenuItem("新建连接标签页");
        addSessionMenuItem = new JMenuItem("添加新会话");
        manageSessionsMenuItem = new JMenuItem("管理会话");
        savedSessionsMenu = new JMenu("已保存的会话");
        closeTabMenuItem = new JMenuItem("关闭当前标签页");
        exitMenuItem = new JMenuItem("退出");
        
        sessionMenu.add(newTabMenuItem);
        sessionMenu.addSeparator();
        sessionMenu.add(addSessionMenuItem);
        sessionMenu.add(manageSessionsMenuItem);
        sessionMenu.add(savedSessionsMenu);
        sessionMenu.addSeparator();
        sessionMenu.add(closeTabMenuItem);
        sessionMenu.addSeparator();
        sessionMenu.add(exitMenuItem);
        
        // 帮助菜单
        helpMenu = new JMenu("帮助");
        aboutMenuItem = new JMenuItem("关于");
        helpMenu.add(aboutMenuItem);
        
        // 主题菜单
        themeMenu = new JMenu("主题");
        themeSettingsMenuItem = new JMenuItem("主题设置...");
        themeSettingsMenuItem.setToolTipText("切换界面主题");
        themeSettingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        themeMenu.add(themeSettingsMenuItem);
        
        // 添加所有菜单到菜单栏
        menuBar.add(sessionMenu);
        menuBar.add(themeMenu);
        menuBar.add(helpMenu);
        
        // 设置菜单栏
        setJMenuBar(menuBar);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 新建标签页菜单项事件
        newTabMenuItem.addActionListener(e -> addNewConnectionTab());
        
        // 添加新会话菜单项事件
        addSessionMenuItem.addActionListener(e -> addNewSession());
        
        // 管理会话菜单项事件
        manageSessionsMenuItem.addActionListener(e -> showSessionManager());
        
        // 关闭标签页菜单项事件
        closeTabMenuItem.addActionListener(e -> closeCurrentTab());
        
        // 退出菜单项事件
        exitMenuItem.addActionListener(e -> exitApplication());
        
        // 关于菜单项事件
        aboutMenuItem.addActionListener(e -> showAboutDialog());
        
        // 主题设置菜单项事件
        themeSettingsMenuItem.addActionListener(e -> ThemeManager.showThemeDialog(this));
        
        // 标签页变更事件
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateMenuBarState();
            }
        });
        
        // 刷新已保存的会话菜单
        refreshSavedSessionsMenu();
    }
    
    /**
     * 添加新的连接标签页
     */
    private void addNewConnectionTab() {
        // 生成唯一的连接名称
        int tabCount = tabbedPane.getTabCount();
        String defaultName = "连接 " + (tabCount + 1);
        
        // 创建新的连接标签页
        ZkConnectionTab newTab = new ZkConnectionTab(defaultName);
        newTab.setCloseListener(new ZkConnectionTab.TabCloseListener() {
            @Override
            public void onTabClose(ZkConnectionTab tab) {
                removeConnectionTab(tab);
            }
        });
        
        // 添加到标签面板
        JPanel tabContentPanel = new JPanel(new BorderLayout());
        tabContentPanel.add(newTab, BorderLayout.CENTER);
        
        tabbedPane.addTab(defaultName, tabContentPanel);
        tabbedPane.setTabComponentAt(tabCount, createTabComponent(defaultName, newTab));
        tabbedPane.setSelectedIndex(tabCount);
        
        logger.info("创建了新的连接标签页: {}", defaultName);
    }
    
    /**
     * 创建带关闭按钮的标签组件
     */
    private Component createTabComponent(String title, ZkConnectionTab tab) {
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        tabPanel.setOpaque(false);
        
        // 标签文本
        JLabel tabLabel = new JLabel(title);
        tabLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        // 创建关闭按钮
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(17, 17));
        closeButton.setToolTipText("关闭连接并关闭标签页");
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setForeground(Color.GRAY);
        
        // 鼠标悬停效果
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(Color.RED);
                closeButton.setContentAreaFilled(true);
                closeButton.setBackground(new Color(240, 240, 240));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(Color.GRAY);
                closeButton.setContentAreaFilled(false);
            }
        });
        
        // 点击关闭按钮时，先断开连接，再关闭tab
        closeButton.addActionListener(e -> {
            logger.info("用户点击tab关闭按钮: {}", title);
            closeTab(tab);
        });
        
        tabPanel.add(tabLabel);
        tabPanel.add(closeButton);
        
        return tabPanel;
    }
    
    /**
     * 添加已保存的会话到标签页
     */
    private void addSavedSessionTab(ZkSession session) {
        // 创建新的连接标签页
        ZkConnectionTab newTab = new ZkConnectionTab(session);
        newTab.setCloseListener(new ZkConnectionTab.TabCloseListener() {
            @Override
            public void onTabClose(ZkConnectionTab tab) {
                removeConnectionTab(tab);
            }
        });
        
        // 添加到标签面板
        JPanel tabContentPanel = new JPanel(new BorderLayout());
        tabContentPanel.add(newTab, BorderLayout.CENTER);
        
        int tabCount = tabbedPane.getTabCount();
        tabbedPane.addTab(session.getAlias(), tabContentPanel);
        tabbedPane.setTabComponentAt(tabCount, createTabComponent(session.getAlias(), newTab));
        tabbedPane.setSelectedIndex(tabCount);
        
        logger.info("创建了新的连接标签页: {}", session.getAlias());
    }
    
    /**
     * 获取当前选中的连接标签页
     */
    private ZkConnectionTab getCurrentConnectionTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            Component tabContent = tabbedPane.getComponentAt(selectedIndex);
            if (tabContent instanceof JPanel) {
                Component[] components = ((JPanel) tabContent).getComponents();
                if (components.length > 0 && components[0] instanceof ZkConnectionTab) {
                    return (ZkConnectionTab) components[0];
                }
            }
        }
        return null;
    }
    
    /**
     * 关闭当前标签页
     */
    private void closeCurrentTab() {
        ZkConnectionTab currentTab = getCurrentConnectionTab();
        if (currentTab != null) {
            closeTab(currentTab);
        }
    }
    
    /**
     * 关闭指定标签页
     */
    private void closeTab(ZkConnectionTab tab) {
        // 断开连接
        tab.closeTab();
        
        logger.info("关闭连接标签页: {}", tab.getConnectionName());
    }
    
    /**
     * 从标签面板移除指定标签页
     */
    private void removeConnectionTab(ZkConnectionTab tab) {
        // 查找并移除标签页
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tabContent = tabbedPane.getComponentAt(i);
            if (tabContent instanceof JPanel) {
                Component[] components = ((JPanel) tabContent).getComponents();
                if (components.length > 0 && components[0] == tab) {
                    tabbedPane.removeTabAt(i);
                    logger.info("已关闭标签页: {}", tab.getConnectionName());
                    break;
                }
            }
        }
        
        // 如果所有标签页都被关闭，添加一个新的
        if (tabbedPane.getTabCount() == 0) {
            addNewConnectionTab();
        }
        
        // 更新菜单栏状态
        updateMenuBarState();
    }
    
    /**
     * 更新菜单栏状态
     */
    private void updateMenuBarState() {
        // 如果没有标签页，禁用相关菜单项
        boolean hasTabs = tabbedPane.getTabCount() > 0;
        closeTabMenuItem.setEnabled(hasTabs);
    }
    
    /**
     * 添加新会话
     */
    private void addNewSession() {
        JTextField aliasField = new JTextField(15);
        JTextField hostField = new JTextField(15);
        JTextField portField = new JTextField(5);
        
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("会话别名:"));
        panel.add(aliasField);
        panel.add(new JLabel("主机地址:"));
        panel.add(hostField);
        panel.add(new JLabel("端口:"));
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "添加新会话", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String alias = aliasField.getText().trim();
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            
            if (!alias.isEmpty() && !host.isEmpty() && !port.isEmpty()) {
                ZkSession session = new ZkSession(alias, host, port);
                ZkSessionManager.getInstance().addSession(session);
                refreshSavedSessionsMenu();
                JOptionPane.showMessageDialog(this, "会话添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "请填写完整的会话信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    /**
     * 显示会话管理器对话框
     */
    private void showSessionManager() {
        JDialog dialog = new JDialog(this, "管理会话", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        // 创建会话列表
        DefaultListModel<ZkSession> listModel = new DefaultListModel<>();
        List<ZkSession> sessions = ZkSessionManager.getInstance().getAllSessions();
        for (ZkSession session : sessions) {
            listModel.addElement(session);
        }
        
        JList<ZkSession> sessionList = new JList<>(listModel);
        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(sessionList);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton connectButton = new JButton("连接");
        JButton editButton = new JButton("编辑");
        JButton deleteButton = new JButton("删除");
        JButton closeButton = new JButton("关闭");
        
        buttonPanel.add(connectButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        
        // 设置按钮事件
        connectButton.addActionListener(e -> {
            ZkSession selected = sessionList.getSelectedValue();
            if (selected != null) {
                addSavedSessionTab(selected);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请选择一个会话", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        editButton.addActionListener(e -> {
            ZkSession selected = sessionList.getSelectedValue();
            if (selected != null) {
                editSession(selected, listModel);
            } else {
                JOptionPane.showMessageDialog(dialog, "请选择一个会话", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            ZkSession selected = sessionList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        dialog, 
                        "确定要删除会话 '" + selected.getAlias() + "' 吗？", 
                        "确认删除", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    ZkSessionManager.getInstance().removeSession(selected);
                    listModel.removeElement(selected);
                    refreshSavedSessionsMenu();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "请选择一个会话", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        // 设置对话框布局
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 显示对话框
        dialog.setVisible(true);
    }
    
    /**
     * 编辑会话
     */
    private void editSession(ZkSession session, DefaultListModel<ZkSession> listModel) {
        JTextField aliasField = new JTextField(session.getAlias(), 15);
        JTextField hostField = new JTextField(session.getHost(), 15);
        JTextField portField = new JTextField(session.getPort(), 5);
        
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("会话别名:"));
        panel.add(aliasField);
        panel.add(new JLabel("主机地址:"));
        panel.add(hostField);
        panel.add(new JLabel("端口:"));
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "编辑会话", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String alias = aliasField.getText().trim();
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            
            if (!alias.isEmpty() && !host.isEmpty() && !port.isEmpty()) {
                ZkSession newSession = new ZkSession(alias, host, port);
                ZkSessionManager.getInstance().updateSession(session, newSession);
                
                // 更新列表模型
                int index = listModel.indexOf(session);
                if (index >= 0) {
                    listModel.set(index, newSession);
                }
                
                refreshSavedSessionsMenu();
            } else {
                JOptionPane.showMessageDialog(this, "请填写完整的会话信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    /**
     * 刷新已保存的会话菜单
     */
    private void refreshSavedSessionsMenu() {
        // 清除现有菜单项
        savedSessionsMenu.removeAll();
        
        // 获取保存的会话列表
        List<ZkSession> sessions = ZkSessionManager.getInstance().getAllSessions();
        
        if (sessions.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("无保存的会话");
            emptyItem.setEnabled(false);
            savedSessionsMenu.add(emptyItem);
        } else {
            // 添加每个保存的会话作为菜单项
            for (ZkSession session : sessions) {
                JMenuItem sessionItem = new JMenuItem(session.toString());
                sessionItem.addActionListener(e -> addSavedSessionTab(session));
                savedSessionsMenu.add(sessionItem);
            }
        }
    }
    
    /**
     * 退出应用程序
     */
    private void exitApplication() {
        // 确认退出
        int confirm = JOptionPane.showConfirmDialog(
                this, 
                "确定要退出ZooKeeper客户端吗？", 
                "确认退出", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            logger.info("用户确认退出，开始关闭所有 ZooKeeper 连接...");
            
            // 关闭所有连接标签页
            closeAllConnections();
            
            // 保存会话信息
            ZkSessionManager.getInstance().saveSessions();
            
            logger.info("ZooKeeper客户端退出");
            System.exit(0);
        } else {
            logger.info("用户取消退出操作");
        }
    }
    
    /**
     * 关闭所有 ZooKeeper 连接
     */
    private void closeAllConnections() {
        int tabCount = tabbedPane.getTabCount();
        logger.info("开始关闭 {} 个连接标签页...", tabCount);
        
        int successCount = 0;
        int failureCount = 0;
        
        // 遍历所有标签页，从后往前关闭（避免索引变化）
        for (int i = tabCount - 1; i >= 0; i--) {
            try {
                Component tabContent = tabbedPane.getComponentAt(i);
                
                if (tabContent instanceof JPanel) {
                    Component[] components = ((JPanel) tabContent).getComponents();
                    
                    if (components.length > 0 && components[0] instanceof ZkConnectionTab) {
                        ZkConnectionTab tab = (ZkConnectionTab) components[0];
                        String connectionName = tab.getConnectionName();
                        
                        logger.info("正在关闭连接 [{}/{}]: {}", 
                            (tabCount - i), tabCount, connectionName);
                        
                        try {
                            // 关闭 ZooKeeper 连接（但不触发标签页关闭回调）
                            tab.disconnectFromZkOnly();
                            
                            logger.info("成功关闭连接: {}", connectionName);
                            successCount++;
                            
                        } catch (Exception e) {
                            logger.error("关闭连接失败: {} - {}", connectionName, e.getMessage(), e);
                            failureCount++;
                        }
                    }
                }
                
            } catch (Exception e) {
                logger.error("处理标签页 {} 时发生错误: {}", i, e.getMessage(), e);
                failureCount++;
            }
        }
        
        logger.info("连接关闭完成 - 成功: {}, 失败: {}, 总计: {}", 
            successCount, failureCount, tabCount);
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this, 
                "ZooKeeper客户端\n版本: 1.0\n\n支持多标签页的ZooKeeper管理工具", 
                "关于", 
                JOptionPane.INFORMATION_MESSAGE);
    }
}
