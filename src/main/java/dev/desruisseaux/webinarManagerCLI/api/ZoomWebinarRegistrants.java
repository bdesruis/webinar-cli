package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebinarRegistrants {
    @JsonProperty("page_count")
    public Long pageCount;

    @JsonProperty("page_number")
    public Long pageNumber;

    @JsonProperty("page_size")
    public Long pageSize;

    @JsonProperty("total_records")
    public Long totalRecords;

    @JsonProperty("registrants")
    public List<ZoomWebinarRegistrant> items = null;
}