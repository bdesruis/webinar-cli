package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebinarCustomQuestion {
    public String title;
    public String value;
}
