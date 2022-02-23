package com.config;

/**
 * @author hcy
 * @since 2022/2/23 15:42
 */
public class ForwardConfig {

    String fromHost;
    int fromPort;

    String toHost;
    int toPort;

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public int getFromPort() {
        return fromPort;
    }

    public void setFromPort(int fromPort) {
        this.fromPort = fromPort;
    }

    public String getToHost() {
        return toHost;
    }

    public void setToHost(String toHost) {
        this.toHost = toHost;
    }

    public int getToPort() {
        return toPort;
    }

    public void setToPort(int toPort) {
        this.toPort = toPort;
    }

    @Override
    public String toString() {
        return "ForwardConfig{" +
                "fromHost='" + fromHost + '\'' +
                ", fromPort=" + fromPort +
                ", toHost='" + toHost + '\'' +
                ", toPort=" + toPort +
                '}';
    }
}
