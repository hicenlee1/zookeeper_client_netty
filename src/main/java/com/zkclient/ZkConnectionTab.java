package com.zkclient;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 表示单个ZooKeeper连接的标签页
 */
public class ZkConnectionTab extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ZkConnectionTab.class);
    
    // 连接相关属性
    private ZkClient zkClient;
    private boolean isConnected = false;
    private String connectionName;
    private String host;
    private String port;
    
    // UI组件
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JTree zkTree;
    private ZkTreeModel treeModel;
    private JTextArea dataTextArea;
    private JTextArea statTextArea;
    private JLabel statusLabel;
    private ExecutorService executorService;
    
    private TabCloseListener closeListener;
    
    /**
     * 用于通知标签页关闭的监听器接口
     */
    public interface TabCloseListener {
        void onTabClose(ZkConnectionTab tab);
    }
    
    /**
     * 构造一个新的连接标签页
     * @param name 连接名称
     */
    public ZkConnectionTab(String name) {
        super(new BorderLayout());
        this.connectionName = name;
        
        executorService = Executors.newFixedThreadPool(5);
        zkClient = new ZkClient();
        treeModel = new ZkTreeModel(zkClient);
        
        initComponents();
        setupLayout();
        setupListeners();
    }
    
    /**
     * 构造一个新的连接标签页，使用已有会话配置
     * @param session 会话配置
     */
    public ZkConnectionTab(ZkSession session) {
        super(new BorderLayout());
        this.connectionName = session.getAlias();
        this.host = session.getHost();
        this.port = session.getPort();
        
        executorService = Executors.newFixedThreadPool(5);
        zkClient = new ZkClient();
        treeModel = new ZkTreeModel(zkClient);
        
        initComponents();
        
        // 设置连接信息
        hostField.setText(host);
        portField.setText(port);
        
        setupLayout();
        setupListeners();
        
        // 自动连接
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logger.info("从保存的会话创建标签页，自动连接: {}", connectionName);
                connectToZooKeeper();
            }
        });
    }
    
    public String getConnectionName() {
        return connectionName;
    }
    
    public void setCloseListener(TabCloseListener listener) {
        this.closeListener = listener;
    }
    
    private void initComponents() {
        // 连接设置面板组件
        hostField = new JTextField("localhost", 15);
        portField = new JTextField("2181", 8);
        connectButton = new JButton("连接");
        
        // 树形结构组件
        zkTree = new JTree(treeModel);
        zkTree.setCellRenderer(new ZkNodeRenderer());
        
        // 数据展示组件
        dataTextArea = new JTextArea();
        dataTextArea.setEditable(false);
        dataTextArea.setLineWrap(true);
        dataTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        dataTextArea.setText("请选择一个节点以查看数据内容");
        
        // 状态展示组件
        statTextArea = new JTextArea();
        statTextArea.setEditable(false);
        statTextArea.setLineWrap(true);
        statTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statTextArea.setText("请选择一个节点以查看属性信息");
        
        // 状态栏
        statusLabel = new JLabel("未连接");
    }
    
    private void setupLayout() {
        // 顶部连接面板
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectPanel.add(new JLabel("主机: "));
        connectPanel.add(hostField);
        connectPanel.add(new JLabel(" 端口: "));
        connectPanel.add(portField);
        connectPanel.add(connectButton);
        
        add(connectPanel, BorderLayout.NORTH);
        
        // 中间主内容面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        
        // 左侧树面板
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("ZooKeeper节点树"), BorderLayout.NORTH);
        JScrollPane treeScrollPane = new JScrollPane(zkTree);
        treeScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(leftPanel);
        
        // 右侧内容面板 - 使用JSplitPane垂直分割面板实现上下布局
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // 节点数据JTextArea，包装在JScrollPane中（放在上半部分）
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(new JLabel("节点数据"), BorderLayout.NORTH);
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
        dataScrollPane.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5), 
            BorderFactory.createLineBorder(Color.GRAY, 1)
        ));
        dataPanel.add(dataScrollPane, BorderLayout.CENTER);
        rightSplitPane.setTopComponent(dataPanel);
        
        // 节点属性JTextArea，包装在JScrollPane中（放在下半部分）
        JPanel statPanel = new JPanel(new BorderLayout());
        statPanel.add(new JLabel("节点属性"), BorderLayout.NORTH);
        JScrollPane statScrollPane = new JScrollPane(statTextArea);
        statScrollPane.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5), 
            BorderFactory.createLineBorder(Color.GRAY, 1)
        ));
        statPanel.add(statScrollPane, BorderLayout.CENTER);
        rightSplitPane.setBottomComponent(statPanel);
        
        // 设置分割线位置
        rightSplitPane.setDividerLocation(250);
        rightSplitPane.setResizeWeight(0.5); // 平均分配空间
        mainSplitPane.setRightComponent(rightSplitPane);
        
        add(mainSplitPane, BorderLayout.CENTER);
        
        // 底部状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
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
                    
                    // 立即显示加载中状态
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DefaultMutableTreeNode loadingNode = new DefaultMutableTreeNode("正在加载...");
                            loadingNodeRef[0] = loadingNode;
                            finalNode.add(loadingNode);
                            treeModel.nodeStructureChanged(finalNode);
                            statusLabel.setText("正在加载子节点...");
                        }
                    });
                    
                    // 在后台线程中加载子节点
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                treeModel.refreshNode(finalNode);
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (loadingNodeRef[0] != null && finalNode.isNodeChild(loadingNodeRef[0])) {
                                            finalNode.remove(loadingNodeRef[0]);
                                        }
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
                    
                    throw new ExpandVetoException(event, "延迟展开直到子节点加载完成");
                }
            }
            
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // 折叠节点不需要特殊处理
            }
        });
        
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
                            isConnected = true;
                            
                            try {
                                treeModel.refreshTree();
                                
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
                            isConnected = false;
                            
                            dataTextArea.setText("");
                            statTextArea.setText("");
                            
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
                            if (data != null) {
                                try {
                                    dataTextArea.setText(new String(data, "UTF-8"));
                                } catch (Exception e) {
                                    dataTextArea.setText("无法解析数据: " + e.getMessage());
                                }
                            } else {
                                dataTextArea.setText("无数据");
                            }
                            
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
     * 关闭标签页（断开连接并通知父窗口）
     */
    public void closeTab() {
        logger.info("关闭标签页: {}", connectionName);
        
        // 先断开连接
        disconnectFromZkOnly();
        
        // 通知父窗口移除此标签页
        if (closeListener != null) {
            closeListener.onTabClose(this);
        }
    }
    
    /**
     * 仅断开ZooKeeper连接（不触发标签页关闭）
     * 用于程序退出时批量关闭所有连接
     */
    public void disconnectFromZkOnly() {
        if (zkClient != null && zkClient.isConnected()) {
            try {
                logger.info("断开ZooKeeper连接: {}", connectionName);
                zkClient.close();
                isConnected = false;
            } catch (Exception e) {
                logger.error("断开连接失败: {}", connectionName, e);
            }
        }
        
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
