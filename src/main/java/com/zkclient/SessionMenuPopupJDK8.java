package com.zkclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SessionMenuPopupJDK8 extends JFrame {

    /* ========================= 数据 ========================= */
    private final List<ZkSession> sessions = new ArrayList<>();
    private final JMenu sessionMenu = new JMenu("会话");
    /* 当前在鼠标下的会话 */
    private ZkSession mouseOverSession;

    public SessionMenuPopupJDK8() {
        super("SessionMenuPopupJDK8");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        /* ---- 假数据 ---- */
        sessions.add(new ZkSession("local", "127.0.0.1", "2181"));
        sessions.add(new ZkSession("test", "192.168.1.100", "2181"));

        /* ---- 菜单栏 ---- */
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        bar.add(sessionMenu);

        /* ---- 初始刷新 ---- */
        refreshSessionMenu();

        /* ---- 占位 ---- */
        add(new JLabel("右键会话看控制台", SwingConstants.CENTER));
    }

    /* ========================= 刷新 ========================= */
    private void refreshSessionMenu() {
        sessionMenu.removeAll();

        /* 静态功能项 */
        JMenuItem refreshItem = new JMenuItem("刷新会话列表");
        refreshItem.addActionListener(e -> refreshSessionMenu());
        sessionMenu.add(refreshItem);
        sessionMenu.addSeparator();

        JMenuItem addItem = new JMenuItem("添加新会话");
        addItem.addActionListener(e -> addNewSession());
        sessionMenu.add(addItem);
        sessionMenu.addSeparator();

        /* 动态会话项：用 JLabel 当“条目”，可以拿到坐标 */
        for (ZkSession s : sessions) {
            JLabel label = new JLabel(s.toString());
            label.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15)); // 看起来像个菜单项
            label.setOpaque(true);

            /* 鼠标进入时记录当前会话 */
            label.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    mouseOverSession = s;
                }
                @Override public void mouseExited(MouseEvent e) {
                    mouseOverSession = null;
                }
            });

            /* 把 JLabel 当成一个“自定义菜单项”塞进 JMenu */
            sessionMenu.add(label);
        }

        /* ---- 给整个 JMenu 加鼠标监听 ---- */
        sessionMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && mouseOverSession != null)
                    showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && mouseOverSession != null)
                    showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                JPopupMenu pop = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("编辑会话");
                editItem.addActionListener(a -> editSession(mouseOverSession));
                pop.add(editItem);

                JMenuItem delItem = new JMenuItem("删除会话");
                delItem.addActionListener(a -> deleteSession(mouseOverSession));
                pop.add(delItem);

                /* 在菜单栏坐标系里显示 */
                pop.show(sessionMenu, e.getX(), e.getY());
            }
        });
    }

    /* -------------------- 业务 -------------------- */
    private void addNewSession() {
        String alias = JOptionPane.showInputDialog(this, "会话别名:");
        if (alias == null || alias.trim().isEmpty()) return;
        String host = JOptionPane.showInputDialog(this, "主机:");
        if (host == null || host.trim().isEmpty()) return;
        String port = JOptionPane.showInputDialog(this, "端口:");
        if (port == null || port.trim().isEmpty()) return;
        sessions.add(new ZkSession(alias.trim(), host.trim(), port.trim()));
        refreshSessionMenu();
    }

    private void editSession(ZkSession old) {
        String alias = JOptionPane.showInputDialog(this, "新别名:", old.getAlias());
        if (alias == null || alias.trim().isEmpty()) return;
        String host = JOptionPane.showInputDialog(this, "新主机:", old.getHost());
        if (host == null || host.trim().isEmpty()) return;
        String port = JOptionPane.showInputDialog(this, "新端口:", old.getPort());
        if (port == null || port.trim().isEmpty()) return;
        sessions.remove(old);
        sessions.add(new ZkSession(alias.trim(), host.trim(), port.trim()));
        refreshSessionMenu();
    }

    private void deleteSession(ZkSession s) {
        int ok = JOptionPane.showConfirmDialog(this,
                "确定删除会话 '" + s.getAlias() + "' ？",
                "确认", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            sessions.remove(s);
            refreshSessionMenu();
        }
    }

    /* ========================= VO ========================= */
    private static class ZkSession {
        private final String alias, host, port;
        ZkSession(String alias, String host, String port) {
            this.alias = alias; this.host = host; this.port = port;
        }
        String getAlias() { return alias; }
        String getHost()  { return host; }
        String getPort()  { return port; }
        @Override
        public String toString() { return alias + " (" + host + ":" + port + ")"; }
    }

    /* ========================= 启动 ========================= */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SessionMenuPopupJDK8().setVisible(true));
    }
}