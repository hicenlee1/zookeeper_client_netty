package com.zkclient;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * 主题管理器 - 负责应用程序的主题切换和管理
 */
public class ThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    // 主题类型枚举
    public enum Theme {
        DARCULA("Darcula (暗色)", FlatDarculaLaf.class.getName()),
        DARK("FlatLaf Dark (深色)", FlatDarkLaf.class.getName()),
        LIGHT("IntelliJ (亮色)", FlatIntelliJLaf.class.getName()),
        FLAT_LIGHT("FlatLaf Light (浅色)", FlatLightLaf.class.getName()),
        SYSTEM("系统默认", UIManager.getSystemLookAndFeelClassName());

        private final String displayName;
        private final String className;

        Theme(String displayName, String className) {
            this.displayName = displayName;
            this.className = className;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getClassName() {
            return className;
        }
    }

    private static final String PREF_THEME_KEY = "selected_theme";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static Theme currentTheme = Theme.LIGHT; // 默认使用 IntelliJ 亮色主题

    /**
     * 初始化主题 - 在应用启动时调用
     */
    public static void initTheme() {
        // 从配置中加载上次使用的主题
        String savedTheme = prefs.get(PREF_THEME_KEY, Theme.LIGHT.name());
        try {
            currentTheme = Theme.valueOf(savedTheme);
        } catch (IllegalArgumentException e) {
            logger.warn("无效的主题配置: {}, 使用默认主题", savedTheme);
            currentTheme = Theme.LIGHT;
        }

        applyTheme(currentTheme);
    }

    /**
     * 应用指定主题
     * @param theme 要应用的主题
     * @return 是否成功应用
     */
    public static boolean applyTheme(Theme theme) {
        try {
            logger.info("正在应用主题: {}", theme.getDisplayName());

            // 设置 FlatLaf 特定属性
            if (theme == Theme.DARCULA || theme == Theme.DARK ||
                theme == Theme.LIGHT || theme == Theme.FLAT_LIGHT) {
                // 启用窗口装饰
                FlatLaf.setUseNativeWindowDecorations(false);

                // 设置菜单栏嵌入窗口标题栏（macOS风格）
                System.setProperty("flatlaf.menuBarEmbedded", "false");

                // 启用动画效果
                UIManager.put("Component.arrowType", "chevron");
                UIManager.put("ScrollBar.showButtons", true);
                UIManager.put("TabbedPane.showTabSeparators", true);
            }

            // 设置 Look and Feel
            UIManager.setLookAndFeel(theme.getClassName());

            // 更新所有已存在的窗口
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.pack();
            }

            // 保存当前主题
            currentTheme = theme;
            prefs.put(PREF_THEME_KEY, theme.name());

            logger.info("主题应用成功: {}", theme.getDisplayName());
            return true;

        } catch (Exception e) {
            logger.error("应用主题失败: {}", theme.getDisplayName(), e);
            JOptionPane.showMessageDialog(null,
                    "应用主题失败: " + e.getMessage(),
                    "主题错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 切换到下一个主题
     */
    public static void switchToNextTheme() {
        Theme[] themes = Theme.values();
        int currentIndex = currentTheme.ordinal();
        int nextIndex = (currentIndex + 1) % themes.length;
        applyTheme(themes[nextIndex]);
    }

    /**
     * 获取当前主题
     * @return 当前主题
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * 获取所有可用主题
     * @return 主题数组
     */
    public static Theme[] getAvailableThemes() {
        return Theme.values();
    }

    /**
     * 显示主题选择对话框
     * @param parent 父窗口
     */
    public static void showThemeDialog(Component parent) {
        Theme[] themes = getAvailableThemes();
        String[] themeNames = new String[themes.length];
        for (int i = 0; i < themes.length; i++) {
            themeNames[i] = themes[i].getDisplayName();
        }

        String currentThemeName = currentTheme.getDisplayName();

        String selected = (String) JOptionPane.showInputDialog(
                parent,
                "选择界面主题：",
                "主题设置",
                JOptionPane.QUESTION_MESSAGE,
                null,
                themeNames,
                currentThemeName
        );

        if (selected != null && !selected.equals(currentThemeName)) {
            for (Theme theme : themes) {
                if (theme.getDisplayName().equals(selected)) {
                    if (applyTheme(theme)) {
                        JOptionPane.showMessageDialog(parent,
                                "主题已更改为: " + theme.getDisplayName(),
                                "主题设置",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
                }
            }
        }
    }
}
