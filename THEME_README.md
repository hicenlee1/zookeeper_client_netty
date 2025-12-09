# ZooKeeper 客户端主题功能说明

## 功能概述

参考 Redis 客户端项目的主题实现方式，为 ZooKeeper 客户端项目添加了 FlatLaf 主题支持，提供现代化的界面体验。

## 主要特性

### 1. 默认主题
- **默认使用 IntelliJ 亮色主题**
- 提供现代化、美观的界面风格
- 与 IntelliJ IDEA 的界面风格一致

### 2. 多主题支持
支持以下 5 种主题：

1. **Darcula (暗色)** - IntelliJ IDEA 经典暗色主题
2. **FlatLaf Dark (深色)** - FlatLaf 深色主题
3. **IntelliJ (亮色)** - IntelliJ IDEA 亮色主题（默认）
4. **FlatLaf Light (浅色)** - FlatLaf 浅色主题
5. **系统默认** - 使用操作系统的原生主题

### 3. 主题切换
- 通过菜单栏 **主题 → 主题设置...** 访问主题选择对话框
- 支持快捷键 **Ctrl+T** 快速打开主题设置
- 主题切换后立即生效，无需重启程序
- 自动更新所有已打开的窗口和组件

### 4. 主题持久化
- 自动保存用户选择的主题
- 下次启动时自动应用上次使用的主题
- 使用 Java Preferences API 存储配置

## 核心类说明

### ThemeManager
**位置**: `src/main/java/com/zkclient/ThemeManager.java`

**功能**: 主题管理器 - 负责应用程序的主题切换和管理

**主要方法**:
- `initTheme()`: 初始化主题，在应用启动时调用
- `applyTheme(Theme theme)`: 应用指定主题
- `switchToNextTheme()`: 切换到下一个主题
- `getCurrentTheme()`: 获取当前主题
- `getAvailableThemes()`: 获取所有可用主题
- `showThemeDialog(Component parent)`: 显示主题选择对话框

**主题枚举**:
```java
public enum Theme {
    DARCULA("Darcula (暗色)", FlatDarculaLaf.class.getName()),
    DARK("FlatLaf Dark (深色)", FlatDarkLaf.class.getName()),
    LIGHT("IntelliJ (亮色)", FlatIntelliJLaf.class.getName()),
    FLAT_LIGHT("FlatLaf Light (浅色)", FlatLightLaf.class.getName()),
    SYSTEM("系统默认", UIManager.getSystemLookAndFeelClassName());
}
```

## 使用说明

### 切换主题

#### 方法 1: 通过菜单
1. 打开 ZooKeeper 客户端
2. 点击菜单栏 → **主题** → **主题设置...**
3. 在弹出的对话框中选择想要的主题
4. 点击确定，主题立即生效

#### 方法 2: 使用快捷键
1. 按 **Ctrl+T** 打开主题设置对话框
2. 选择想要的主题
3. 点击确定

### 主题效果

- **Darcula (暗色)**: 深色背景，护眼舒适，适合长时间使用
- **FlatLaf Dark (深色)**: 现代深色主题，更加扁平化
- **IntelliJ (亮色)**: 明亮清晰，适合光线充足的环境
- **FlatLaf Light (浅色)**: 简洁的浅色主题
- **系统默认**: 与操作系统风格保持一致

## 技术实现

### 1. FlatLaf 依赖
在 `pom.xml` 中添加了 FlatLaf 库依赖：

```xml
<!-- FlatLaf - 现代化 Swing 主题库 -->
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf</artifactId>
    <version>3.2.5</version>
</dependency>
```

### 2. 主题初始化
在 `MainApp.java` 和 `MultiTabZkClient.java` 中调用 `ThemeManager.initTheme()` 初始化主题：

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        try {
            // 初始化并应用主题
            ThemeManager.initTheme();
            
            // 创建并显示主窗口
            MultiTabZkClient client = new MultiTabZkClient();
            client.setVisible(true);
        } catch (Exception e) {
            // 错误处理
        }
    });
}
```

### 3. 主题应用
主题应用时会自动：
- 设置 FlatLaf 特定属性（窗口装饰、动画效果等）
- 更新 UIManager 的 Look and Feel
- 刷新所有已存在的窗口和组件
- 保存主题选择到配置文件

### 4. UI 组件更新
```java
// 更新所有已存在的窗口
for (Window window : Window.getWindows()) {
    SwingUtilities.updateComponentTreeUI(window);
    window.pack();
}
```

## 配置文件

主题配置使用 Java Preferences API 存储，位置：
- **Windows**: `HKEY_CURRENT_USER\Software\JavaSoft\Prefs\com\zkclient`
- **Linux**: `~/.java/.userPrefs/com/zkclient`
- **macOS**: `~/Library/Preferences/com.zkclient.plist`

配置键：`selected_theme`

## 与现有代码的集成

### 1. 保持一致性
- 使用与 Redis 客户端相同的 ThemeManager 实现
- 遵循相同的设计模式和代码风格
- 使用相同的 FlatLaf 库版本

### 2. 无侵入性
- 不影响现有功能
- 只在启动时初始化主题
- 主题切换完全独立

### 3. 扩展性
- 易于添加新主题
- 支持自定义主题配置
- 可以扩展主题选项

## 优势特点

### 1. 现代化界面
- 使用 FlatLaf 提供的现代化 UI 组件
- 界面更加美观、扁平化
- 与主流 IDE 风格一致

### 2. 用户体验
- 支持多种主题，满足不同用户偏好
- 主题切换即时生效
- 自动保存用户选择

### 3. 跨平台一致性
- 在不同操作系统上保持一致的界面风格
- 避免系统默认主题的差异

### 4. 护眼功能
- Darcula 暗色主题减少眼睛疲劳
- 适合长时间使用

## 注意事项

1. **首次使用**: 首次启动时使用 IntelliJ 亮色主题作为默认主题
2. **主题保存**: 更改主题后会自动保存，下次启动时自动应用
3. **组件更新**: 主题切换后会自动更新所有已打开的窗口
4. **性能影响**: 主题切换时可能会有短暂的界面重绘，属于正常现象

## 后续优化建议

1. 添加更多自定义主题选项
2. 支持导入/导出主题配置
3. 添加主题预览功能
4. 支持自定义颜色方案
5. 添加主题快速切换按钮
6. 支持主题动画过渡效果

## 版本信息

- **FlatLaf 版本**: 3.2.5
- **支持的主题数**: 5 个
- **默认主题**: IntelliJ (亮色)
- **配置方式**: Java Preferences API

## 截图说明

不同主题下的界面效果：

1. **IntelliJ (亮色)** - 默认主题，明亮清晰
2. **Darcula (暗色)** - 深色主题，护眼舒适
3. **FlatLaf Dark (深色)** - 现代深色风格
4. **FlatLaf Light (浅色)** - 简洁浅色风格
5. **系统默认** - 与操作系统保持一致
