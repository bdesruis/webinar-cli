package dev.desruisseaux.webinarManagerCLI.oauth;

import com.auth0.jwt.JWT;
import dev.desruisseaux.webinarManagerCLI.client.ZoomOAuthClient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ZoomOAuthTokenSupplier {
    private ZoomOAuthClient zoomOAuthClient;
    private String accountId;
    private String jwtToken = null;
    private long jwtTokenExpiresAtMillis = 0L;

    public ZoomOAuthTokenSupplier(ZoomOAuthClient zoomOAuthClient, String accountId) {
        this.zoomOAuthClient = zoomOAuthClient;
        this.accountId = accountId;
    };

    public synchronized String getAccessToken() throws IOException {
        if (jwtToken != null) {
            // Check token is still valid for 60 seconds
            if (jwtTokenExpiresAtMillis - System.currentTimeMillis() < 60000L) {
                jwtToken = null;
            }
        }

        if (jwtToken == null) {
            ZoomOAuthToken token = zoomOAuthClient.token().create("account_credentials", this.accountId).execute();
            jwtToken = token.getAccessToken();
            jwtTokenExpiresAtMillis = JWT.decode(jwtToken).getExpiresAt().getTime();
        }
        return jwtToken;
    }
}