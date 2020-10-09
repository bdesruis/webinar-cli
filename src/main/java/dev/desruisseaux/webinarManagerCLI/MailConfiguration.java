package dev.desruisseaux.webinarManagerCLI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailConfiguration {
    @NotEmpty
    private EmailAddressConfig from;
    @NotEmpty
    private String fromPassword;
    private EmailAddressConfig replyTo;
    private EmailAddressConfig bcc;
    private EmailAddressConfig listUnsubscribe;
    @NotEmpty
    private String subject;
    @NotEmpty
    private String textPlainContent;
    @NotEmpty
    private String textHtmlContent;
    @NotEmpty
    private String renewalNotice;
    @NotEmpty
    private long renewalNoticePeriodInDays;
    private String expiryTableRow;
    @NotEmpty
    private String smtpHost;
    @NotEmpty
    private Integer smtpPort;
    @NotEmpty
    private Boolean smtpSslEnable;
    @NotEmpty
    private Boolean smtpAuth;
    @NotEmpty
    private Boolean debug;
}