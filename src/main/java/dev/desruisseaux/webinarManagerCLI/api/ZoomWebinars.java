package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 * Doc: https://marketplace.zoom.us/docs/api-reference/zoom-api/webinars/webinars
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebinars {
    @JsonProperty("page_count")
    private Long pageCount;

    @JsonProperty("page_number")
    private Long pageNumber;

    @JsonProperty("page_size")
    private Long pageSize;

    @JsonProperty("total_records")
    private Long totalRecords;

    @JsonProperty("webinars")
    private List<ZoomWebinar> items;
}