package com.doodle.chat.server;

import spark.Session;
import spark.Spark;

import java.net.URLDecoder;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.notFound;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;
import static spark.Spark.webSocket;

public class Server {

    private static int SERVER_ERROR = 100;
    private static Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Queue<spark.Session> sessions = new ConcurrentLinkedQueue<spark.Session>();
    private final WebSocketHandler wsHandler = new WebSocketHandler(sessions);

    public void start() {
        port(9001);
        staticFileLocation("/public");

        initExceptionHandler(e -> {
            LOGGER.log(Level.SEVERE, "Failed to initialize websocket server", e);
            System.exit(SERVER_ERROR);
        });

        webSocket("/messages", wsHandler);

        get("/", (request, response) -> {
            response.redirect("/login");
            return null;
        });

        get("/login", (request, response) -> {
            response.redirect("/login.html");
            return null;
        });

        post("/auth", (request, response) -> {
            final String bodySplit[] = URLDecoder.decode(request.body(),"UTF-8").split("=");
            if (bodySplit.length != 2) {
                response.redirect("/login");
                return null;
            }
            if (!bodySplit[0].equals("username")) {
                response.redirect("/login");
                return null;
            }

            final String username = bodySplit[1];
            final Optional<Session> existingUsername = sessions.stream()
                    .filter(session -> session.attribute("username").equals(username))
                    .findAny();

            if (existingUsername.isPresent()) {
                response.redirect("/login");
                return null;
            }

            final Session session = request.session(true);
            session.attribute("username", username);
            sessions.add(session);
            response.cookie("username", username);
            response.redirect("/application");
            halt();
            return null;
        });

        get("/messages", (request, response) -> null);

        get("/application", (request, response) -> {
            final Map<String, String> cookies = request.cookies();
            final String jSessionIDExtended = cookies.get("JSESSIONID");
            final String username = cookies.get("username");

            if (jSessionIDExtended == null || username == null) {
                response.redirect("/login");
                return null;
            }

            final String jSessionID = jSessionIDExtended.substring(0, jSessionIDExtended.indexOf("."));
            final Optional<Session> existingJSessionID =
                    sessions.stream()
                            .filter(session -> session.id().equals(jSessionID))
                            .findAny();

            final Optional<Session> existingUsername =
                    sessions.stream()
                            .filter(session -> session.attribute("username").equals(username))
                            .findAny();

            if (!existingJSessionID.isPresent() || !existingUsername.isPresent()) {
                response.redirect("/login");
                return null;
            }
            response.redirect("./application.html");
            return null;
        });
        get("/login.html", (request, response) -> {
            System.out.println("HHHH");
            return null;
        });
        notFound("/not_found.html");

        get("*", (request, response) -> {
            response.redirect("not_found.html");
            return null;
        });
    }

    public void stop() {
        Spark.stop();
    }
}
