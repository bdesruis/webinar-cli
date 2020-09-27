package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.model.property.Contact;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomContacts {
    @JsonProperty("page_size")
    private Integer pageSize;
    @JsonProperty("next_page_token")
    private String nextPageToken;
    @JsonProperty("contacts")
    private List<ZoomContact> items;
}