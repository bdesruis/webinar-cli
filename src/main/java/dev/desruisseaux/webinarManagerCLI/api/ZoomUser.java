package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/*
 * Doc: https://marketplace.zoom.us/docs/api-reference/zoom-api/users/user
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomUser {
    private String id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private Long type;

    @JsonProperty("role_name")
    private String roleName;

    private Long pmi;

    @JsonProperty("use_pmi")
    private Boolean usePmi;

    @JsonProperty("personal_meeting_url")
    private String personalMeetingUrl;

    private String timezone;

    private Long verified;

    private String dept;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
    @JsonProperty("created_at")
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
    @JsonProperty("last_login_time")
    private Date lastLoginTime;

    @JsonProperty("last_client_version")
    private String lastClientVersion;

    @JsonProperty("pic_url")
    private String picUrl;

    @JsonProperty("host_key")
    private String hostKey;

    private String jid;

    @JsonProperty("group_ids")
    private List<String> groupIds;

    @JsonProperty("im_group_ids")
    private List<String> imGroupIds;

    @JsonProperty("account_id")
    private String accountId;

    private String language;

    @JsonProperty("phone_country")
    private String phoneCountry;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String status;
}