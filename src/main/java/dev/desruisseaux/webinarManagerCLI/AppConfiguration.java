package dev.desruisseaux.webinarManagerCLI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfiguration {
    @NotEmpty
    private ZoomConfiguration zoom;
    @NotEmpty
    private CalendarConfiguration calendar;
    @NotEmpty
    private MailConfiguration mail;
}