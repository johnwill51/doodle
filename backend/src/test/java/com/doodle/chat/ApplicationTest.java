package com.doodle.chat;

import com.doodle.chat.domain.Message;
import com.google.gson.Gson;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApplicationTest {

    private final Gson gson = new Gson();

    private final String message1 =
            "{" +
                    "\"author\":\"New\"," +
                    "\"datetime\":123," +
                    "\"content\":\"The tide is high\"" +
                    "}";
    private final String message2 =
            "{" +
                    "\"author\":\"Dubby\"," +
                    "\"datetime\":456," +
                    "\"content\":\"but I'm holding on\"" +
                    "}";
    private final String message3 =
            "{" +
                    "\"author\":\"Conquerors\"," +
                    "\"datetime\":789," +
                    "\"content\":\"Ja, ja, yeah, yeah\"" +
                    "}";

    private ChatHttpClient chatHttpClient;
    private ChatWebSocketClient chatWebSocketClient;
    private CountDownLatch countDownLatch;
    private List<String> messagesToSend;
    private Queue<String> messagesReceived;
    private Queue<Integer> closedReceived;

    @BeforeClass
    public static void serverStart() throws InterruptedException {
        Application.main(null);
    }

    @AfterClass
    public static void serverStop() {
        Application.stop();
    }

    @Before
    public void setupClients() throws Exception {
        // TODO: find a clean way to wait for the previous test to complete
        // add on stop interceptors in http and websocket clients
        Thread.sleep(1000);

        chatHttpClient = new ChatHttpClient();
        chatHttpClient.start();

        countDownLatch = null;
        messagesToSend = null;
        messagesReceived = null;
        closedReceived = null;
    }

    @After
    public void teardownClients() throws Exception {
        if (chatWebSocketClient != null) {
            chatWebSocketClient.stop();
            chatWebSocketClient = null;
        }
        if (chatHttpClient != null) {
            chatHttpClient.stop();
            chatHttpClient = null;
        }
    }

    @Test
    public void test_11_WhenClientGetInvalidPageRedirectToNotFound() throws
            Exception {
        final ContentResponse response = chatHttpClient.get("http://localhost:9001/a");
        assertEquals("/not_found.html", response.getRequest().getPath());
    }

    @Test
    public void test_12_WhenClientGetHomeRedirectToLogin() throws Exception {
        final ContentResponse response = chatHttpClient.get("http://localhost:9001/");
        assertEquals("/login.html", response.getRequest().getPath());
    }

    @Test
    public void test_13_WhenClientPostAuthInvalidUsernameRedirectToLogin()
            throws InterruptedException, ExecutionException, TimeoutException {
        final ContentResponse response =
                chatHttpClient.post("http://localhost:9001/auth", "a=b");
        assertEquals("/login.html", response.getRequest().getPath());
    }

    @Test
    public void test_14_WhenClientPostAuthNoUsernameRedirectToLogin()
            throws InterruptedException, ExecutionException, TimeoutException {
        final ContentResponse response =
                chatHttpClient.post("http://localhost:9001/auth", "");
        assertEquals("/login.html", response.getRequest().getPath());
    }

    @Test
    public void test_15_WhenClientPostAuthValidUsernameRedirectToApplication()
            throws InterruptedException, ExecutionException, TimeoutException {
        final ContentResponse response =
                chatHttpClient.post("http://localhost:9001/auth", "will");

        final String responseCookie = response.getRequest().getHeaders().get("Cookie");
        final String username = responseCookie.split(";")[1].trim().split("=")[1];

        assertEquals("/application.html", response.getRequest().getPath());
        assertEquals("will", username);
    }

    @Test
    public void test_16_WhenClientPostSameAuthRedirectToLogin()
            throws Exception {
        final ContentResponse response =
                chatHttpClient.post("http://localhost:9001/auth", "will");
        assertEquals("/login.html", response.getRequest().getPath());
    }

    @Test
    public void test_17_WhenClientGetApplicationWithoutAuthRedirectToLogin()
            throws InterruptedException, ExecutionException, TimeoutException {
        final ContentResponse response =
                chatHttpClient.get("http://localhost:9001/application");
        assertEquals("/login.html", response.getRequest().getPath());
    }

    @Test
    public void test_18_WhenClientGetApplicationWithAuthProceeds()
            throws InterruptedException, ExecutionException, TimeoutException, URISyntaxException {
        final List<HttpCookie> cookies = authenticateChatHttpClient(chatHttpClient, "_18");
        final ContentResponse response =
                chatHttpClient.get("http://localhost:9001/application", cookies);
        assertEquals("/application.html", response.getRequest().getPath());
    }

    @Test
    public void test_21_WhenClientSendMessageReceiveOwnMessage() throws Exception {
        final List<HttpCookie> cookies = authenticateChatHttpClient(chatHttpClient, "_21");

        countDownLatch = new CountDownLatch(2);
        messagesToSend = new ArrayList<String>() {{ add(message1); add(message2); }};
        messagesReceived = new ConcurrentLinkedQueue<>();

        chatWebSocketClient = createChatWebSocketClient();
        chatWebSocketClient.start(cookies);
        countDownLatch.await(5, TimeUnit.SECONDS);

        assertEquals(2, messagesReceived.size());
        assertMessageEquals(message1, messagesReceived.poll());
        assertMessageEquals(message2, messagesReceived.poll());
    }

    @Test
    public void test_22_WhenClientConnectReceivePreviousMessages() throws Exception {
        final List<HttpCookie> cookies = authenticateChatHttpClient(chatHttpClient, "_22");

        countDownLatch = new CountDownLatch(2);
        messagesReceived = new ConcurrentLinkedQueue<>();

        chatWebSocketClient = createChatWebSocketClient();
        chatWebSocketClient.start(cookies);
        countDownLatch.await(5, TimeUnit.SECONDS);

        assertEquals(2, messagesReceived.size());
        assertMessageEquals(message1, messagesReceived.poll());
        assertMessageEquals(message2, messagesReceived.poll());
    }

    @Test
    public void test_23_WhenClientSendMessageOtherClientReceiveMessage() throws Exception {
        final ChatHttpClient chatHttpClient1 = new ChatHttpClient();
        chatHttpClient1.start();
        final List<HttpCookie> cookies1 = authenticateChatHttpClient(chatHttpClient1, "_23_1");

        final ChatHttpClient chatHttpClient2 = new ChatHttpClient();
        chatHttpClient2.start();
        final List<HttpCookie> cookies2 = authenticateChatHttpClient(chatHttpClient2, "_23_2");

        countDownLatch = new CountDownLatch(3);
        messagesReceived = new ConcurrentLinkedQueue<>();

        final ChatWebSocketClient chatWebSocketClient1 = createChatWebSocketClient();
        chatWebSocketClient1.start(cookies1);

        messagesToSend = new ArrayList<String>() {{ add(message3); }};

        final ChatWebSocketClient chatWebSocketClient2 =
                createChatWebSocketClient(messagesToSend, null, null, null);
        chatWebSocketClient2.start(cookies2);

        countDownLatch.await(5, TimeUnit.SECONDS);

        chatWebSocketClient1.stop();
        chatWebSocketClient2.stop();
        chatHttpClient1.stop();
        chatHttpClient2.stop();

        assertEquals(3, messagesReceived.size());
        assertMessageEquals(message1, messagesReceived.poll());
        assertMessageEquals(message2, messagesReceived.poll());
        assertMessageEquals(message3, messagesReceived.poll());
    }

    @Test
    public void test_24_WhenClientConnectNoAuthReceiveDisconnect() throws Exception {
        countDownLatch = new CountDownLatch(1);
        closedReceived = new ConcurrentLinkedQueue<>();

        chatWebSocketClient = createChatWebSocketClient();
        chatWebSocketClient.start(null);
        countDownLatch.await(5, TimeUnit.SECONDS);

        assertEquals(1, closedReceived.size());
        assertEquals(new Integer(1000), closedReceived.poll());
    }

    private boolean assertMessageEquals(final String a, final String b) {
        final Message messageA = gson.fromJson(a, Message.class);
        final Message messageB = gson.fromJson(b, Message.class);
        return (
                messageA.author.equals(messageB.author) &&
                        messageA.datetime == messageB.datetime &&
                        messageA.content.equals(messageB.content)
        );
    }

    private List<HttpCookie> authenticateChatHttpClient(
            final ChatHttpClient chClient,
            final String name
    )
            throws InterruptedException, ExecutionException, TimeoutException {
        final ContentResponse responseAuth =
                chClient.post("http://localhost:9001/auth", name);

        final String responseCookie = responseAuth.getRequest().getHeaders().get("Cookie");
        final String jSessionID = responseCookie.split(";")[0].trim().split("=")[1];
        final String username = responseCookie.split(";")[1].trim().split("=")[1];

        return new ArrayList<HttpCookie>() {{
            add(new HttpCookie("JSESSIONID", jSessionID));
            add(new HttpCookie("username", username));
        }};
    }

    private ChatWebSocketClient createChatWebSocketClient() {
        return new ChatWebSocketClient(
                new ChatWebSocketHandler(
                        messagesToSend,
                        messagesReceived,
                        closedReceived,
                        countDownLatch
                )
        );
    }

    private ChatWebSocketClient createChatWebSocketClient(
            final List<String> overrideMessagesToSend,
            final Queue<String> overrideMessagesReceived,
            final Queue<Integer> overrideClosedReceived,
            final CountDownLatch overrideCountDownLatch
    ) {
        return new ChatWebSocketClient(
                new ChatWebSocketHandler(
                        overrideMessagesToSend,
                        overrideMessagesReceived,
                        overrideClosedReceived,
                        overrideCountDownLatch
                )
        );
    }

}
