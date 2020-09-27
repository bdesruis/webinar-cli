package dev.desruisseaux.webinarManagerCLI.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomWebinarRegistrant {
    public String id;
    public String email;
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("last_name")
    public String lastName;
    public String address;
    public String city;
    public String country;
    public String zip;
    public String state;
    public String phone;
    public String industry;
    public String org;
    @JsonProperty("job_title")
    public String jobTitle;
    @JsonProperty("purchasing_time_frame")
    public String purchasingTimeFrame;
    @JsonProperty("role_in_purchase_process")
    public String roleInPurchaseProcess;
    @JsonProperty("no_of_employees")
    public String noOfEmployees;
    public String comments;
    @JsonProperty("custom_questions")
    public List<ZoomWebinarCustomQuestion> customQuestions = null;
    public String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @JsonProperty("create_time")
    public Date createTime;
    @JsonProperty("join_url")
    public String joinUrl;
}