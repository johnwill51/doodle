package com.doodle.chat;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Fields.Field;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class ChatHttpClient {

    private final HttpClient client = new HttpClient();

    ChatHttpClient() {}

    void start() throws Exception {
        client.start();
    }

    void stop() throws Exception {
        client.stop();
    }

    ContentResponse get(final String url)
            throws InterruptedException, ExecutionException, TimeoutException {
        return client.GET(url);
    }

    ContentResponse get(final String url, final List<HttpCookie> cookies)
            throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
        final URI uri = new URI(url);
        cookies.stream().forEach(cookie -> client.getCookieStore().add(uri, cookie));
        return client.GET(url);
    }

    ContentResponse post(final String url, final String username)
            throws InterruptedException, ExecutionException, TimeoutException {
        final Field usernameField = new Field("username", username);
        final Fields fields = new Fields() {{ put(usernameField); }};
        return client.FORM(url, fields);
    }

}
