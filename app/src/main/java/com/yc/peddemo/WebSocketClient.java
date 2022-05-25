package com.yc.peddemo;

import android.util.Log;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    private String TAG = "WebSocketClient";

    public WebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "onOpen handshakedata=$handshakedata");
    }

    @Override
    public void onMessage(String message) {
        Log.i(TAG, "onMessage message=$message");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "onClose code=$code reason=$reason remote=$remote");
    }

    @Override
    public void onError(Exception ex) {
        Log.i(TAG, "onError ex=$ex");
    }

}
