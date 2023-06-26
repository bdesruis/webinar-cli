package dev.desruisseaux.webinarManagerCLI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomConfiguration {
    @NotEmpty
    private String jwtToken;
    private String apiBaseUrl;
    private String oAuthBaseUrl;
    private String clientId;
    private String clientSecret;
    private String accountId;
    private Boolean maskPanelistEmail = true;
}