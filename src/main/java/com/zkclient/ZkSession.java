package com.zkclient;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 表示ZooKeeper会话的类，用于存储连接信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZkSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String alias;       // 会话别名，用于显示
    private String host;        // ZooKeeper主机地址
    private String port;        // ZooKeeper端口
    private boolean isDefault;  // 是否为默认会话
    
    // 无参构造函数，用于Jackson反序列化
    public ZkSession() {
        // 默认构造函数，Jackson需要这个构造函数来反序列化对象
    }
    
    public ZkSession(String alias, String host, String port) {
        this.alias = alias;
        this.host = host;
        this.port = port;
        this.isDefault = false;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    @JsonIgnore
    public String getConnectString() {
        return host + ":" + port;
    }
    
    @Override
    public String toString() {
        return alias + " (" + getConnectString() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ZkSession that = (ZkSession) obj;
        return host.equals(that.host) && port.equals(that.port);
    }
    
    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }
}