package dev.desruisseaux.webinarManagerCLI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomConfiguration {
    @NotEmpty
    private String apiBaseUrl;
    @NotEmpty
    private String jwtToken;
    @NotEmpty
    private String userEmail;
    @NotEmpty
    private String userId;
}