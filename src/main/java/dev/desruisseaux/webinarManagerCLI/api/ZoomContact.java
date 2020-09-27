package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomContact {
    private String id;
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("presence_status")
    private String presenceStatus;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("sip_phone_number")
    private String sipPhoneNumber;
    @JsonProperty("direct_numbers")
    private List<Integer> directNumbers = null;
    @JsonProperty("extension_number")
    private String extensionNumber;
    @JsonProperty("im_group_id")
    private String imGroupId;
    @JsonProperty("im_group_name")
    private String imGroupName;
}
