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
    private Boolean maskPanelistEmail = true;
}