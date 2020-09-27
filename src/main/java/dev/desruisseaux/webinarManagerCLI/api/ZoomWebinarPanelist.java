package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ZoomWebinarPanelist {
    private String id;
    private String name;
    private String email;
    @JsonProperty("join_url")
    private String joinUrl;

    public ZoomWebinarPanelist(String name, String email) {
        this.name = name;
        this.email = email;
    }
}