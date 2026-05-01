package itz.silentcore.web.proxy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProxyConfig {
    private ProxyType type = ProxyType.NONE;
    private String host = "";
    private int port = 8080;
    private String username = "";
    private String password = "";

    public boolean isEnabled() {
        return type != ProxyType.NONE && !host.isEmpty() && port > 0;
    }

    public enum ProxyType {
        NONE("None"),
        SOCKS4("SOCKS4"),
        SOCKS5("SOCKS5"),
        HTTPS("HTTPS");

        @Getter
        private final String displayName;

        ProxyType(String displayName) {
            this.displayName = displayName;
        }
    }
}