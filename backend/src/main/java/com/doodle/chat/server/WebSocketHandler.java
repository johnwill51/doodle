package com.doodle.chat.server;

import com.doodle.chat.domain.Message;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class WebSocketHandler {

    private static Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final Gson gson = new Gson();

    private final Queue<Message> messages = new ConcurrentLinkedQueue<>();
    private final Queue<Session> wsSessions = new ConcurrentLinkedQueue<>();
    private final Queue<spark.Session> serverSessions;

    public WebSocketHandler(final Queue<spark.Session> serverSessions) {
        this.serverSessions = serverSessions;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        final List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();

        if (cookies == null) {
            session.close();
            return;
        }

        final Optional<HttpCookie> jSessionIDCookie =
                cookies.stream()
                        .filter(cookie -> cookie.getName().equals("JSESSIONID"))
                        .findAny();
        final Optional<HttpCookie> usernameCookie =
                cookies.stream()
                        .filter(cookie -> cookie.getName().equals("username"))
                        .findAny();

        if (!jSessionIDCookie.isPresent() || !usernameCookie.isPresent()) {
            session.close();
            return;
        }

        final String jSessionIDExtended = jSessionIDCookie.get().getValue();
        final String jSessionID = jSessionIDExtended.substring(0, jSessionIDExtended.indexOf("."));
        final Optional<spark.Session> existingJSessionID =
                serverSessions.stream()
                        .filter(serverSession -> serverSession.id().equals(jSessionID))
                        .findAny();

        final String username = usernameCookie.get().getValue();
        final Optional<spark.Session> existingUsername =
                serverSessions.stream()
                        .filter(serverSession -> serverSession.attribute("username").equals(username))
                        .findAny();

        if (!existingJSessionID.isPresent() || !existingUsername.isPresent()) {
            session.close();
            return;
        }

        wsSessions.add(session);
        sendAllMessagesToSession(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        wsSessions.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String messageAsString) throws IOException {
        final Message message = gson.fromJson(messageAsString, Message.class);
        messages.add(message);
        broadcast(message);
    }

    private void sendAllMessagesToSession(Session session) {
        messages.stream().forEach(message -> {
            try {
                session.getRemote().sendString(
                        String.valueOf(gson.toJson(message))
                );
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "broadcast message failed", e);
            }
        });
    }

    private void broadcast(final Message message) {
        final String messageAsString = String.valueOf(gson.toJson(message));
        wsSessions.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(messageAsString);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "broadcast message failed", e);
            }
        });
    }
}
