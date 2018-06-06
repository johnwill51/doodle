package com.doodle.chat;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class ChatWebSocketHandler
{
    private final List<String> messagesToSendOnConnect;
    private final Queue<String> messagesReceived;
    private final Queue<Integer> closeReceived;
    private final CountDownLatch countDownLatch;

    ChatWebSocketHandler(
            final List<String> messagesToSendOnConnect,
            final Queue<String> messagesReceived,
            final Queue<Integer> closeReceived,
            final CountDownLatch countDownLatch
    ) {
        this.messagesToSendOnConnect = messagesToSendOnConnect;
        this.messagesReceived = messagesReceived;
        this.closeReceived = closeReceived;
        this.countDownLatch = countDownLatch;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (closeReceived != null) {
            closeReceived.add(new Integer(statusCode));
            countDownLatch.countDown();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        if (messagesToSendOnConnect != null)
            messagesToSendOnConnect.forEach(message -> {
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {}
            });
    }

    @OnWebSocketMessage
    public void onMessage(final String message) {
        if (messagesReceived != null)
            messagesReceived.add(message);
        if (countDownLatch != null)
            countDownLatch.countDown();
    }
}