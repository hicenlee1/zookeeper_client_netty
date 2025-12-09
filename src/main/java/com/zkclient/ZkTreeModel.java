package com.zkclient;

import org.apache.zookeeper.KeeperException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * ZooKeeper树形模型，用于JTree组件展示ZK节点结构
 */
public class ZkTreeModel extends DefaultTreeModel {
    private ZkClient zkClient;

    public ZkTreeModel(ZkClient zkClient) {
        super(new DefaultMutableTreeNode("根节点"));
        this.zkClient = zkClient;
        refreshTree();
    }

    /**
     * 刷新整棵树
     */
    public void refreshTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ZkNode("/", "/"));
        
        // 先加载子节点，再设置为根节点
        if (zkClient != null && zkClient.isConnected()) {
            try {
                loadChildren(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        setRoot(root);
        nodeStructureChanged((TreeNode) getRoot());
    }

    /**
     * 加载指定节点的子节点（只加载一层，不递归加载所有层级）
     * @param parentNode 父节点
     */
    private void loadChildren(DefaultMutableTreeNode parentNode) throws KeeperException, InterruptedException {
        ZkNode parentZkNode = (ZkNode) parentNode.getUserObject();
        String parentPath = parentZkNode.getPath();
        
        // 清除当前节点的所有子节点
        parentNode.removeAllChildren();
        
        List<String> children = zkClient.getChildren(parentPath);
        if (children != null && !children.isEmpty()) {
            parentZkNode.setLeaf(false);
            for (String childName : children) {
                String childPath = parentPath.equals("/") ? "/" + childName : parentPath + "/" + childName;
                ZkNode childZkNode = new ZkNode(childPath, childName);
                // 创建子节点，但不加载其子节点
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childZkNode);
                parentNode.add(childNode);
            }
        } else {
            parentZkNode.setLeaf(true);
        }
    }
    
    /**
     * 检查节点是否有子节点（用于判断节点是否可以展开）
     * @param node 要检查的节点
     * @return 是否有子节点
     */
    public boolean hasChildren(DefaultMutableTreeNode node) {
        if (node == null || !(node.getUserObject() instanceof ZkNode)) {
            return false;
        }
        
        // 已经加载过子节点的情况
        if (node.getChildCount() > 0) {
            return true;
        }
        
        // 未加载过子节点的情况，需要检查是否真的有子节点
        try {
            ZkNode zkNode = (ZkNode) node.getUserObject();
            List<String> children = zkClient.getChildren(zkNode.getPath());
            return children != null && !children.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新指定节点
     * @param node 要刷新的节点
     */
    public void refreshNode(DefaultMutableTreeNode node) {
        if (node == null || !(node.getUserObject() instanceof ZkNode)) {
            return;
        }
        
        ZkNode zkNode = (ZkNode) node.getUserObject();
        node.removeAllChildren();
        
        if (zkClient != null && zkClient.isConnected()) {
            try {
                loadChildren(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        nodeStructureChanged(node);
    }
    
    /**
     * 重写isLeaf方法，确保正确识别可展开的节点
     * 即使节点的子节点还没有被加载，只要ZooKeeper中实际存在子节点，就认为它不是叶子节点
     */
    @Override
    public boolean isLeaf(Object node) {
        if (node instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            // 对于根节点特殊处理
            if (treeNode.isRoot()) {
                return false;  // 根节点永远不是叶子节点
            }
            return !hasChildren(treeNode);
        }
        return super.isLeaf(node);
    }
    
    /**
     * 重写getChildCount方法，确保正确返回子节点数量
     */
    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
            // 对于根节点特殊处理 - 确保即使还没有加载子节点也能显示为可展开
            if (parentNode.isRoot()) {
                // 已经加载过子节点的情况
                if (parentNode.getChildCount() > 0) {
                    return parentNode.getChildCount();
                }
                // 未加载过子节点的情况，只要连接成功，就显示为可展开
                if (zkClient != null && zkClient.isConnected()) {
                    return 1;
                }
            } else {
                // 已经加载过子节点的非根节点，返回实际的子节点数量
                if (parentNode.getChildCount() > 0) {
                    return parentNode.getChildCount();
                }
                // 未加载过子节点但实际存在子节点的非根节点，返回1（用于显示展开图标）
                if (hasChildren(parentNode)) {
                    return 1;
                }
            }
        }
        return super.getChildCount(parent);
    }

    /**
     * 设置ZooKeeper客户端
     * @param zkClient ZooKeeper客户端
     */
    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
        refreshTree();
    }
}