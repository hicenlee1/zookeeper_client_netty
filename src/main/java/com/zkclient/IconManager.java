package com.zkclient;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 图标管理器类，用于加载和管理应用程序中的图标资源
 */
public class IconManager {
    // 单例实例
    private static IconManager instance;
    
    // 图标缓存
    private Map<String, ImageIcon> iconCache;
    
    // 私有构造函数
    private IconManager() {
        iconCache = new HashMap<>();
    }
    
    /**
     * 获取图标管理器单例实例
     * @return IconManager实例
     */
    public static synchronized IconManager getInstance() {
        if (instance == null) {
            instance = new IconManager();
        }
        return instance;
    }
    
    /**
     * 获取应用程序窗口图标
     * @return 窗口图标
     */
    public ImageIcon getApplicationIcon() {
        String iconKey = "app_icon_32";
        ImageIcon icon = iconCache.get(iconKey);
        
        if (icon == null) {
            // 首先尝试从资源文件加载图标
            icon = loadIconFromResources("/icons/zk_icon_32.png");
            
            // 如果资源文件不存在，则动态创建图标
            if (icon == null) {
                icon = createZooKeeperIcon(32, Color.BLUE, Color.WHITE);
            }
            
            // 缓存图标
            iconCache.put(iconKey, icon);
        }
        
        return icon;
    }
    
    /**
     * 从资源文件加载图标
     * @param resourcePath 资源路径
     * @return ImageIcon对象，如果加载失败返回null
     */
    private ImageIcon loadIconFromResources(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                ImageIcon icon = new ImageIcon(javax.imageio.ImageIO.read(is));
                return icon;
            }
        } catch (IOException e) {
            // 加载失败，返回null
        }
        return null;
    }
    
    /**
     * 创建ZooKeeper图标
     * @param size 图标尺寸
     * @param primaryColor 主要颜色
     * @param secondaryColor 次要颜色
     * @return ImageIcon对象
     */
    private ImageIcon createZooKeeperIcon(int size, Color primaryColor, Color secondaryColor) {
        // 创建一个图像
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        // 设置抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制背景圆形
        g2.setColor(primaryColor);
        Ellipse2D circle = new Ellipse2D.Double(2, 2, size - 4, size - 4);
        g2.fill(circle);
        
        // 绘制边框
        g2.setColor(primaryColor.darker());
        g2.draw(circle);
        
        // 绘制ZK字母
        g2.setColor(secondaryColor);
        g2.setFont(new Font("Arial", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String text = "ZK";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
        
        g2.dispose();
        
        return new ImageIcon(image);
    }
}