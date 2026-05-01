package itz.silentcore.web.proxy;

import lombok.Getter;

@Getter
public class ProxyManager {
    private static final ProxyManager instance = new ProxyManager();
    private ProxyConfig config = new ProxyConfig();

    public static ProxyManager getInstance() {
        return instance;
    }

    public void updateConfig(ProxyConfig.ProxyType type, String host, int port, String username, String password) {
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setUsername(username);
        config.setPassword(password);
    }

    public void reset() {
        config = new ProxyConfig();
    }
}