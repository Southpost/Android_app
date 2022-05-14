package com.yc.peddemo.event;

/**
 * @author: lei
 * @date: 2022/3/19
 **/
public class ConnectEvent {
    private boolean connect;

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public ConnectEvent(boolean connect) {
        this.connect = connect;
    }
}
