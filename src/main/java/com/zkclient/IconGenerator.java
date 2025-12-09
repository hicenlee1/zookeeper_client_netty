package com.zkclient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Icon generator class for generating and saving icon files to the resources directory
 */
public class IconGenerator {
    
    /**
     * Generate and save ZooKeeper icon to the specified path
     * @param size icon size
     * @param outputPath output file path
     */
    public static void generateAndSaveZooKeeperIcon(int size, String outputPath) {
        try {
            // Create icon
            BufferedImage image = createZooKeeperIcon(size, Color.BLUE, Color.WHITE);
            
            // Ensure output directory exists
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Save icon to file
            ImageIO.write(image, "png", outputFile);
            System.out.println("Icon successfully generated and saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to generate icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create ZooKeeper icon
     * @param size icon size
     * @param primaryColor primary color
     * @param secondaryColor secondary color
     * @return BufferedImage object
     */
    private static BufferedImage createZooKeeperIcon(int size, Color primaryColor, Color secondaryColor) {
        // Create an image
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        // Set anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background circle
        g2.setColor(primaryColor);
        Ellipse2D circle = new Ellipse2D.Double(2, 2, size - 4, size - 4);
        g2.fill(circle);
        
        // Draw border
        g2.setColor(primaryColor.darker());
        g2.draw(circle);
        
        // Draw ZK letters
        g2.setColor(secondaryColor);
        g2.setFont(new Font("Arial", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String text = "ZK";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
        
        g2.dispose();
        
        return image;
    }
    
    /**
     * Main method for generating icon files
     */
    public static void main(String[] args) {
        // Generate icons of different sizes
        generateAndSaveZooKeeperIcon(16, "src/main/resources/icons/zk_icon_16.png");
        generateAndSaveZooKeeperIcon(32, "src/main/resources/icons/zk_icon_32.png");
        generateAndSaveZooKeeperIcon(48, "src/main/resources/icons/zk_icon_48.png");
        generateAndSaveZooKeeperIcon(64, "src/main/resources/icons/zk_icon_64.png");
        
        System.out.println("All icons generated successfully!");
    }
}