package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/*
 * Doc: https://marketplace.zoom.us/docs/api-reference/zoom-api/webinars/webinar
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebinar implements Comparable<ZoomWebinar> {
    private String uuid;

    private Long id;

    // ID of the user who is set as the host of the webinar
    @JsonProperty("host_id")
    private String hostId;

    private String topic;

    // Webinar Type
    // 5 - Webinar
    // 6 - Recurring Webinar without a fixed time
    // 9 - Recurring Webinar with a fixed time
    private Long type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
    @JsonProperty("start_time")
    private Date startTime;

    private Long duration;

    private String timezone;

    // Occurrences - Returned only if webinar type is 9
    @JsonProperty("occurrence")
    private List<ZoomWebinarOccurrence> occurences;

    //
    // Following properties are NOT returned in Webinar ZoomWebhookWebinarEventRequest.payload.object
    //
    private String agenda;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone="UTC")
    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("start_url")
    private String startUrl;

    @JsonProperty("join_url")
    private String joinUrl;

    @JsonProperty("registration_url")
    private String registrationUrl;

    private String password;

    public String toString() {
        return "Webinar(" + this.getId() + ", " + this.getStartTime() + ", " + this.getTopic() + ")";
    }

    @Override
    public int compareTo(ZoomWebinar w) {
        return this.startTime.compareTo(w.getStartTime());
    }
}