package itz.silentcore.web.server;

import com.google.gson.JsonObject;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.client.Log;
import itz.silentcore.web.proxy.ProxyConfig;
import itz.silentcore.web.proxy.ProxyManager;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerConnection {
    private static final ServerConnection INSTANCE = new ServerConnection();
    private static final int TIMEOUT_SECONDS = 8;
    private static final String SERVER_URL = Constants.SERVER;
    private static final int MAX_RECONNECT_DELAY = 30000;
    private static final int HEARTBEAT_INTERVAL_SECONDS = 30;
    private static final int WAIT_FOR_CONNECTION_MS = 2500;

    private volatile WebSocket webSocket;
    private volatile boolean connected = false;
    private volatile boolean connecting = false;
    private volatile HttpClient httpClient;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger connectionAttempts = new AtomicInteger(0);
    private volatile long lastConnectionAttempt = 0;
    private final ProxyManager proxyManager = ProxyManager.getInstance();
    private volatile ScheduledExecutorService heartbeatScheduler;

    private ServerConnection() {
        updateHttpClient();
        initiateConnection();
    }

    public static ServerConnection getInstance() {
        return INSTANCE;
    }

    private void updateHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5));

        ProxyConfig proxyConfig = proxyManager.getConfig();
        if (proxyConfig.isEnabled()) {
            Proxy proxy = createProxy(proxyConfig);
            if (proxy != null) {
                java.net.ProxySelector proxySelector = new java.net.ProxySelector() {
                    @Override
                    public java.util.List<Proxy> select(URI uri) {
                        return java.util.Collections.singletonList(proxy);
                    }

                    @Override
                    public void connectFailed(URI uri, java.net.SocketAddress sa, java.io.IOException ioe) {
                    }
                };
                builder.proxy(proxySelector);
            }
        }

        httpClient = builder.build();
    }

    private Proxy createProxy(ProxyConfig config) {
        if (config.getType() == ProxyConfig.ProxyType.NONE || !config.isEnabled()) {
            return null;
        }

        InetSocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());
        Proxy.Type proxyType = switch (config.getType()) {
            case SOCKS4, SOCKS5 -> Proxy.Type.SOCKS;
            case HTTPS -> Proxy.Type.HTTP;
            default -> Proxy.Type.DIRECT;
        };

        return new Proxy(proxyType, address);
    }

    private void initiateConnection() {
        new Thread(this::connect, "ServerConnection-Connect").start();
    }

    private void connect() {
        if (connecting) {
            return;
        }

        connecting = true;
        lastConnectionAttempt = System.currentTimeMillis();

        try {
            httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(SERVER_URL), new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket ws) {
                            webSocket = ws;
                            connected = true;
                            connecting = false;
                            connectionAttempts.set(0);
                            //Log.print("ServerConnection established");
                            startHeartbeat();
                            ws.request(1);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                            String message = data.toString();
                            responseQueue.offer(message);
                            ws.request(1);
                            return null;
                        }

                        @Override
                        public CompletionStage<?> onPong(WebSocket ws, ByteBuffer message) {
                            ws.request(1);
                            return null;
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                            connected = false;
                            connecting = false;
                            stopHeartbeat();
                            Log.print("ServerConnection closed");
                            scheduleReconnectWithBackoff();
                            return null;
                        }

                        @Override
                        public void onError(WebSocket ws, Throwable error) {
                            connected = false;
                            connecting = false;
                            stopHeartbeat();
                            Log.error("ServerConnection error: " + error.getMessage());
                            scheduleReconnectWithBackoff();
                        }
                    }).exceptionally(throwable -> {
                        Log.error("ServerConnection failed: " + throwable.getMessage());
                        connected = false;
                        connecting = false;
                        stopHeartbeat();
                        scheduleReconnectWithBackoff();
                        return null;
                    });
        } catch (Exception e) {
            Log.error("ServerConnection init error: " + e.getMessage());
            connected = false;
            connecting = false;
            scheduleReconnectWithBackoff();
        }
    }

    private void scheduleReconnect(long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                if (!connected) {
                    connect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ServerConnection-Reconnect").start();
    }

    private void scheduleReconnectWithBackoff() {
        int attempts = connectionAttempts.incrementAndGet();
        long delayMs = Math.min(1000L * (long) Math.pow(2, attempts - 1), MAX_RECONNECT_DELAY);
        Log.print("Reconnecting in " + delayMs + "ms (attempt " + attempts + ")");
        scheduleReconnect(delayMs);
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "ServerConnection-Heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
            try {
                if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            heartbeatScheduler = null;
        }
    }

    private void sendHeartbeat() {
        try {
            if (webSocket != null && connected) {
                webSocket.sendPing(ByteBuffer.allocate(0));
            }
        } catch (Exception e) {
            Log.error("Ошибка при подключении к серверу. Попробуйте позже или обратитесь в поддержку: " + Constants.SUPPORT);
        }
    }

    public String sendRequest(JsonObject request) throws Exception {
        long startTime = System.currentTimeMillis();

        while (!connected && (System.currentTimeMillis() - startTime) < WAIT_FOR_CONNECTION_MS) {
            Thread.sleep(250);
        }

        if (!connected) {
            throw new Exception("Ошибка при подключении к серверу. Скорее всего, сейчас идут технические работы.");
        }

        try {
            String requestStr = request.toString();
            webSocket.sendText(requestStr, true);

            String response = responseQueue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (response == null) {
                connected = false;
                scheduleReconnectWithBackoff();
                throw new Exception("Server did not respond within " + TIMEOUT_SECONDS + " seconds");
            }
            return response;
        } catch (Exception e) {
            if (!e.getMessage().contains("did not respond")) {
                Log.error("ServerConnection request failed: " + e.getMessage());
            }
            connected = false;
            scheduleReconnectWithBackoff();
            throw e;
        }
    }

    public void disconnect() {
        connected = false;
        stopHeartbeat();
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").join();
            } catch (Exception e) {
                Log.error("ServerConnection close error: " + e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return connected && webSocket != null;
    }
}