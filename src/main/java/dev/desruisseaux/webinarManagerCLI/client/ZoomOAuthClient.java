package dev.desruisseaux.webinarManagerCLI.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.desruisseaux.webinarManagerCLI.ZoomConfiguration;
import dev.desruisseaux.webinarManagerCLI.oauth.ZoomOAuthToken;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class ZoomOAuthClient {
    private final static ThreadLocal<ObjectMapper> mapperLocal = new ThreadLocal<>();
    private final String oAuthBaseUrl;
    private final String clientId;
    private final String clientSecret;

    public static synchronized ObjectMapper getMapper() {
        ObjectMapper mapper = mapperLocal.get();
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapperLocal.set(mapper);
        }
        return mapper;
    }

    private ZoomOAuthClient(String oAuthBaseUrl, String clientId, String clientSecret) {
        this.oAuthBaseUrl = oAuthBaseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static ZoomOAuthClient.ZoomOAuthClientBuilder builder() {
        return new ZoomOAuthClient.ZoomOAuthClientBuilder();
    }

    public ZoomOAuthClient.Token token() {
        return new ZoomOAuthClient.Token();
    }

    public static class ZoomOAuthClientBuilder {
        private static final String defaultOAuthBaseUrl = "https://zoom.us/oauth";
        private String oAuthBaseUrl;
        private boolean oAuthBaseUrl$set;
        private String clientId;
        private String clientSecret;

        ZoomOAuthClientBuilder() {
        }

        public ZoomOAuthClient.ZoomOAuthClientBuilder fromConfig(ZoomConfiguration config) {
            if (StringUtils.isNotEmpty(config.getOAuthBaseUrl())) {
                this.oAuthBaseUrl = config.getOAuthBaseUrl();
                this.oAuthBaseUrl$set = true;
            }
            this.clientId = config.getClientId();
            this.clientSecret = config.getClientSecret();
            return this;
        }

        public ZoomOAuthClient.ZoomOAuthClientBuilder oAuthBaseUrl(String oAuthBaseUrl) {
            this.oAuthBaseUrl = oAuthBaseUrl;
            this.oAuthBaseUrl$set = true;
            return this;
        }

        public ZoomOAuthClient.ZoomOAuthClientBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ZoomOAuthClient.ZoomOAuthClientBuilder clientSecret(String clientSecret) {
            this.clientId = clientSecret;
            return this;
        }

        public ZoomOAuthClient build() {
            return new ZoomOAuthClient(oAuthBaseUrl$set ? oAuthBaseUrl : defaultOAuthBaseUrl, clientId, clientSecret);
        }
    }

    public class Token {
        public Token() {
        }

        public ZoomOAuthClient.Token.Create create(String grantType, String accountId) {
            return new ZoomOAuthClient.Token.Create(grantType, accountId);
        }

        public class Create {
            private final String grantType;
            private final String accountId;
            public Create(String grantType, String accountId) {
                this.grantType = grantType;
                this.accountId = accountId;
            }
            private String getBasicAuthenticationHeader(String username, String password) {
                String valueToEncode = username + ":" + password;
                return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
            }

            public ZoomOAuthToken execute() throws IOException {
                String url = ZoomOAuthClient.this.oAuthBaseUrl + "/token?grant_type=" + grantType + "&account_id=" + accountId;
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", getBasicAuthenticationHeader(clientId, clientSecret));
                conn.setRequestProperty("Accept", "application/json");
                InputStream is = conn.getInputStream();
                return getMapper().readValue(is, ZoomOAuthToken.class);
            }
        }
    }
}
