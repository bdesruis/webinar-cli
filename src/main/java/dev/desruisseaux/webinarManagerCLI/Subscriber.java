package dev.desruisseaux.webinarManagerCLI;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subscriber {
    @NotEmpty
    @JsonProperty("Prénom")
    private String firstName;
    @NotEmpty
    @JsonProperty("Nom")
    private String lastName;
    @NotEmpty
    @JsonProperty("Courriel")
    private String email;
    @JsonProperty("Expiration")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone="UTC")
    private Date expiryDate;
}