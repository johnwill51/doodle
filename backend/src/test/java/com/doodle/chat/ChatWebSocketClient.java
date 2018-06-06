package com.doodle.chat;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

/**
 * WebSocket client
 */
class ChatWebSocketClient
{
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketClient client = new WebSocketClient();
    private URI wsUri;

    ChatWebSocketClient(final ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    void start(final List<HttpCookie> cookies) throws Exception {
        wsUri = new URI("ws://localhost:9001/messages");
        client.start();
        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setRequestURI(new URI("ws://jose:hello@localhost:9001/messages"));
        request.setHeader("user", "jose");
        request.setCookies(cookies);
        client.connect(chatWebSocketHandler, wsUri, request);
    }

    void stop() throws Exception {
        client.stop();
    }
}
