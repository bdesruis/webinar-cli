package dev.desruisseaux.webinarManagerCLI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarConfiguration {
    @NotEmpty
    private String descriptionPrefix;
    @NotEmpty
    private String descriptionSuffix;
}