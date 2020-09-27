package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ZoomWebinarPanelists {
    @JsonProperty("total_records")
    private Long totalRecords;
    @JsonProperty("panelists")
    private List<ZoomWebinarPanelist> items;

    public ZoomWebinarPanelists (List<ZoomWebinarPanelist> items) {
        this.items = items;
        this.totalRecords = (long) items.size();
    }
}
