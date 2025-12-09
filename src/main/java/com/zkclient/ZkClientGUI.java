package com.zkclient;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

/**
 * ZooKeeper客户端GUI主界面
 */
public class ZkClientGUI extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(ZkClientGUI.class);
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JTree zkTree;
    private ZkTreeModel treeModel;
    private JTextArea dataTextArea;
    private JTextArea statTextArea;
    private JLabel statusLabel;
    private ZkClient zkClient;
    private ExecutorService executorService;
    private JMenu sessionMenu; // 会话菜单
    private JMenuBar menuBar; // 菜单栏

    public ZkClientGUI() {
        super("ZooKeeper客户端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 添加窗口关闭监听器，确保退出时保存会话
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // 调用cleanup方法释放资源
                cleanup();
                // 显式保存会话信息
                ZkSessionManager.getInstance().saveSessions();
            }
        });
        
        executorService = Executors.newFixedThreadPool(5);
        zkClient = new ZkClient();
        treeModel = new ZkTreeModel(zkClient);
        
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        // 连接设置面板组件
        hostField = new JTextField("localhost");
        portField = new JTextField("2181");
        connectButton = new JButton("连接");
        
        // 树形结构组件
        zkTree = new JTree(treeModel);
        zkTree.setCellRenderer(new ZkNodeRenderer());
        //JScrollPane treeScrollPane = new JScrollPane(zkTree);
        
        // 数据展示组件
        dataTextArea = new JTextArea();
        dataTextArea.setEditable(false);
        dataTextArea.setLineWrap(true);
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
        dataScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // 状态展示组件
        statTextArea = new JTextArea();
        statTextArea.setEditable(false);
        statTextArea.setLineWrap(true);
        JScrollPane statScrollPane = new JScrollPane(statTextArea);
        statScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // 状态栏
        statusLabel = new JLabel("未连接");
        
        // 初始化菜单栏
        initMenuBar();
    }

    private void setupLayout() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);
        
        // 顶部连接面板
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectPanel.add(new JLabel("主机: "));
        connectPanel.add(hostField);
        connectPanel.add(new JLabel(" 端口: "));
        connectPanel.add(portField);
        connectPanel.add(connectButton);
        
        // 添加编辑会话按钮
        JButton editSessionButton = new JButton("编辑会话");
        editSessionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 查找当前显示的会话
                String currentHost = hostField.getText().trim();
                String currentPort = portField.getText().trim();
                List<ZkSession> sessions = ZkSessionManager.getInstance().getAllSessions();
                for (ZkSession session : sessions) {
                    if (session.getHost().equals(currentHost) && session.getPort().equals(currentPort)) {
                        editSession(session);
                        return;
                    }
                }
                // 如果没有找到匹配的会话，提示用户
                JOptionPane.showMessageDialog(ZkClientGUI.this, 
                        "未找到与当前主机和端口匹配的会话", 
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        connectPanel.add(editSessionButton);
        
        // 添加删除会话按钮
        JButton deleteSessionButton = new JButton("删除会话");
        deleteSessionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 查找当前显示的会话
                String currentHost = hostField.getText().trim();
                String currentPort = portField.getText().trim();
                List<ZkSession> sessions = ZkSessionManager.getInstance().getAllSessions();
                for (ZkSession session : sessions) {
                    if (session.getHost().equals(currentHost) && session.getPort().equals(currentPort)) {
                        deleteSession(session);
                        return;
                    }
                }
                // 如果没有找到匹配的会话，提示用户
                JOptionPane.showMessageDialog(ZkClientGUI.this, 
                        "未找到与当前主机和端口匹配的会话", 
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        connectPanel.add(deleteSessionButton);
        
        mainPanel.add(connectPanel, BorderLayout.NORTH);
        
        // 中间主内容面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        
        // 左侧树面板
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setTopComponent(new JLabel("ZooKeeper节点树"));
        leftSplitPane.setBottomComponent(new JScrollPane(zkTree));
        leftSplitPane.setDividerLocation(25);
        leftSplitPane.setEnabled(false);
        mainSplitPane.setLeftComponent(leftSplitPane);
        
        // 右侧内容面板
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setTopComponent(new JLabel("节点数据"));
        rightSplitPane.setBottomComponent(new JLabel("节点属性"));
        rightSplitPane.setDividerLocation(250);
        rightSplitPane.setEnabled(false);
        
        // 右侧数据和属性面板
        JPanel rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.add(rightSplitPane, BorderLayout.NORTH);
        rightContentPanel.add(dataTextArea, BorderLayout.CENTER);
        rightContentPanel.add(statTextArea, BorderLayout.SOUTH);
        mainSplitPane.setRightComponent(rightContentPanel);
        
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        
        // 底部状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * 初始化菜单栏
     */
    private void initMenuBar() {
        menuBar = new JMenuBar();
        
        // 创建会话菜单
        sessionMenu = new JMenu("会话");
        menuBar.add(sessionMenu);
        
        // 添加刷新会话菜单项
        JMenuItem refreshMenuItem = new JMenuItem("刷新会话列表");
        refreshMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshSessionMenu();
            }
        });
        sessionMenu.add(refreshMenuItem);
        
        // 添加分隔符
        sessionMenu.addSeparator();
        
        // 添加"添加新会话"菜单项
        JMenuItem addSessionMenuItem = new JMenuItem("添加新会话");
        addSessionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewSession();
            }
        });
        sessionMenu.add(addSessionMenuItem);
        
        // 添加分隔符
        sessionMenu.addSeparator();
        
        // 设置菜单栏
        setJMenuBar(menuBar);
        
        // 初始加载会话列表
        refreshSessionMenu();
    }
    
    /**
     * 刷新会话菜单
     */
    private void refreshSessionMenu() {
        // 先移除所有会话菜单项（保留前三个：刷新、分隔符、添加新会话）
        while (sessionMenu.getItemCount() > 3) {
            sessionMenu.remove(3);
        }
        
        // 添加保存的会话
        List<ZkSession> sessions = ZkSessionManager.getInstance().getAllSessions();
        for (final ZkSession session : sessions) {
            JMenuItem sessionItem = new JMenuItem(session.toString());
            
            // 添加右键菜单
            final JPopupMenu popupMenu = new JPopupMenu();
            
            // 编辑会话菜单项
            JMenuItem editMenuItem = new JMenuItem("编辑会话");
            editMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editSession(session);
                }
            });
            popupMenu.add(editMenuItem);
            
            // 删除会话菜单项
            JMenuItem deleteMenuItem = new JMenuItem("删除会话");
            deleteMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("删除会话菜单项被点击: " + session.getAlias());
                    deleteSession(session);
                }
            });
            popupMenu.add(deleteMenuItem);
            // 确保菜单项是启用的
            deleteMenuItem.setEnabled(true);
            
            // 使用MouseListener来分别处理左键和右键点击
            sessionItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                        // 左键点击：执行连接操作
                        selectSession(session);
                    } else if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                        // 右键点击：显示弹出菜单
                        showPopupMenu(e, popupMenu);
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    // 右键释放：显示弹出菜单（确保在所有平台上都能正常工作）
                    if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                        showPopupMenu(e, popupMenu);
                    }
                }
                
                private void showPopupMenu(MouseEvent e, final JPopupMenu menu) {
                    final Component component = e.getComponent();
                    final int x = e.getX();
                    final int y = e.getY();
                    
                    System.out.println("showPopupMenu被调用，组件: " + component.getClass().getName() + ", 位置: " + x + ", " + y);
                    
                    // 确保组件已显示在屏幕上并且菜单不为空
                    if (component.isShowing() && menu.getComponentCount() > 0) {
                        System.out.println("显示弹出菜单，菜单项数量: " + menu.getComponentCount());
                        // 直接显示菜单，不使用异步处理
                        menu.show(component, x, y);
                    } else {
                        System.out.println("不显示弹出菜单，组件显示状态: " + component.isShowing() + ", 菜单项数量: " + menu.getComponentCount());
                    }
                }
            });
            
            sessionMenu.add(sessionItem);
        }
    }
    
    /**
     * 选择会话
     */
    private void selectSession(ZkSession session) {
        // 先检查当前是否已连接
        if (zkClient != null && zkClient.isConnected()) {
            // 如果已连接，先断开当前连接
            try {
                zkClient.close();
                statusLabel.setText("已断开连接");
                connectButton.setText("连接");
                connectButton.setEnabled(true);
            } catch (Exception e) {
                logger.error("断开当前连接失败", e);
            }
        }
        
        // 设置主机和端口
        hostField.setText(session.getHost());
        portField.setText(session.getPort());
        
        // 自动触发连接操作
        connectToZooKeeper();
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
                refreshSessionMenu();
            } else {
                JOptionPane.showMessageDialog(this, "请填写完整的会话信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    /**
     * 编辑会话
     */
    private void editSession(final ZkSession session) {
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
                refreshSessionMenu();
            } else {
                JOptionPane.showMessageDialog(this, "请填写完整的会话信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    /**
     * 删除会话
     */
    private void deleteSession(ZkSession session) {
        System.out.println("deleteSession方法被调用，会话: " + session.getAlias());
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要删除会话 '" + session.getAlias() + "' 吗？", 
                "确认删除", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            System.out.println("用户确认删除会话: " + session.getAlias());
            ZkSessionManager.getInstance().removeSession(session);
            refreshSessionMenu();
        } else {
            System.out.println("用户取消删除会话: " + session.getAlias());
        }
    }
    
    private void setupListeners() {
        // 连接按钮监听器
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zkClient != null && zkClient.isConnected()) {
                    // 如果已连接，则断开连接
                    disconnectFromZooKeeper();
                } else {
                    // 如果未连接，则连接
                    connectToZooKeeper();
                }
            }
        });
        
        // 树选择监听器
        zkTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectedPath = e.getNewLeadSelectionPath();
                if (selectedPath != null) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                    if (selectedNode.getUserObject() instanceof ZkNode) {
                        ZkNode zkNode = (ZkNode) selectedNode.getUserObject();
                        loadNodeData(zkNode);
                    }
                }
            }
        });
        
        // 树展开监听器 - 实现按需加载子节点
        zkTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                final TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                
                // 如果节点还没有加载过子节点
                if (node.getUserObject() instanceof ZkNode && node.getChildCount() == 0) {
                    final DefaultMutableTreeNode finalNode = node;
                    final DefaultMutableTreeNode[] loadingNodeRef = new DefaultMutableTreeNode[1];
                    
                    // 立即显示加载中状态，避免空白
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // 添加一个临时的"加载中"节点作为反馈
                            DefaultMutableTreeNode loadingNode = new DefaultMutableTreeNode("正在加载...");
                            loadingNodeRef[0] = loadingNode;
                            finalNode.add(loadingNode);
                            treeModel.nodeStructureChanged(finalNode);
                            statusLabel.setText("正在加载子节点...");
                        }
                    });
                    
                    // 在后台线程中加载子节点，避免UI卡顿
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 加载当前节点的直接子节点
                                treeModel.refreshNode(finalNode);
                                
                                // 在事件调度线程中更新UI
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 移除临时加载节点
                                        if (loadingNodeRef[0] != null && finalNode.isNodeChild(loadingNodeRef[0])) {
                                            finalNode.remove(loadingNodeRef[0]);
                                        }
                                        // 展开节点以显示新加载的子节点
                                        zkTree.expandPath(path);
                                        statusLabel.setText("已连接到 " + hostField.getText().trim() + ":" + portField.getText().trim());
                                    }
                                });
                            } catch (Exception e) {
                                logger.error("加载子节点失败", e);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusLabel.setText("加载子节点失败");
                                    }
                                });
                            }
                        }
                    });
                    
                    // 阻止默认的展开行为，因为我们将在子节点加载完成后手动展开
                    throw new ExpandVetoException(event, "延迟展开直到子节点加载完成");
                }
            }
            
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // 折叠节点不需要特殊处理
            }
        });
        
        // 确保树能够正确识别可展开的节点
        zkTree.setShowsRootHandles(true);
        zkTree.setRootVisible(true);
    }

    /**
     * 连接到ZooKeeper服务器
     */
    private void connectToZooKeeper() {
        final String host = hostField.getText().trim();
        final String port = portField.getText().trim();
        final String connectString = host + ":" + port;
        
        connectButton.setEnabled(false);
        statusLabel.setText("正在连接到 " + connectString + "...");

        //final boolean connected = zkClient.connect(connectString);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final boolean connected = zkClient.connect(connectString);
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (connected) {
                                    statusLabel.setText("已连接到 " + connectString);
                                    connectButton.setText("断开连接");
                                    connectButton.setEnabled(true);
                            
                            try {
                                // 刷新树模型
                                treeModel.refreshTree();
                                
                                // 不要立即展开根节点，让树展开监听器来处理
                                // 这样可以避免在子节点尚未完全加载时尝试访问它们导致的异常
                                
                                // 显示根节点数据
                                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
                                if (rootNode.getUserObject() instanceof ZkNode) {
                                    loadNodeData((ZkNode) rootNode.getUserObject());
                                }
                            } catch (Exception e) {
                                statusLabel.setText("刷新树结构失败: " + e.getMessage());
                                logger.error("刷新树结构失败", e);
                            }
                        } else {
                            statusLabel.setText("连接失败: " + connectString);
                            connectButton.setEnabled(true);
                        }
                    }
                });
            }
        });
    }
    
    /**
     * 从ZooKeeper服务器断开连接
     */
    private void disconnectFromZooKeeper() {
        if (zkClient != null && zkClient.isConnected()) {
            statusLabel.setText("正在断开连接...");
            connectButton.setEnabled(false);
            
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        zkClient.close();
                    } catch (Exception e) {
                        logger.error("断开连接失败", e);
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            statusLabel.setText("未连接");
                            connectButton.setText("连接");
                            connectButton.setEnabled(true);
                            
                            // 清空数据显示区域
                            dataTextArea.setText("");
                            statTextArea.setText("");
                            
                            // 重置树模型
                            try {
                                treeModel.refreshTree();
                            } catch (Exception e) {
                                logger.error("重置树模型失败", e);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 加载节点数据和属性
     * @param zkNode 要加载的节点
     */
    private void loadNodeData(final ZkNode zkNode) {
        if (!zkClient.isConnected()) {
            return;
        }
        
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final Stat stat = new Stat();
                    final byte[] data = zkClient.getData(zkNode.getPath(), stat);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // 显示节点数据
                            if (data != null) {
                                try {
                                    dataTextArea.setText(new String(data, "UTF-8"));
                                } catch (Exception e) {
                                    dataTextArea.setText("无法解析数据: " + e.getMessage());
                                }
                            } else {
                                dataTextArea.setText("无数据");
                            }
                            
                            // 显示节点属性
                            StringBuilder statBuilder = new StringBuilder();
                            statBuilder.append("版本: " + stat.getVersion() + "\n");
                            statBuilder.append("创建时间: " + new java.util.Date(stat.getCtime()) + "\n");
                            statBuilder.append("修改时间: " + new java.util.Date(stat.getMtime()) + "\n");
                            statBuilder.append("子节点数: " + stat.getNumChildren() + "\n");
                            statBuilder.append("数据长度: " + stat.getDataLength() + "\n");
                            statBuilder.append("会话ID: " + stat.getEphemeralOwner() + "\n");
                            statTextArea.setText(statBuilder.toString());
                        }
                    });
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dataTextArea.setText("加载数据失败: " + e.getMessage());
                            statTextArea.setText("加载属性失败: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    /**
     * 自定义节点渲染器
     */
    private class ZkNodeRenderer extends javax.swing.tree.DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof ZkNode) {
                    ZkNode zkNode = (ZkNode) node.getUserObject();
                    // 可以根据节点类型设置不同的图标
                }
            }
            
            return this;
        }
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (zkClient != null) {
            zkClient.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}