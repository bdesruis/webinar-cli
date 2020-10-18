package dev.desruisseaux.webinarManagerCLI;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinar;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarPanelist;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarPanelists;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarPanelistsCreateResponse;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinars;
import dev.desruisseaux.webinarManagerCLI.client.ZoomClient;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import static java.lang.Math.abs;

@Slf4j
public class App implements Runnable {
    private AppConfiguration appConfig;
    private ZoomClient zoomClient;
    private Session mailSession;
    final private ZoneId localZoneId = ZoneId.systemDefault();

    @Parameter(names = "--config", description = "Configuration file name")
    private String configFileName;

    @Parameter(names = "--webinarId", description = "Select webinar with this id")
    private List<Long> webinardIds = new ArrayList<>();
    @Parameter(names = "--webinarTopicIncludeRegExp", description = "Include webinars with a topic that matches this regexp")
    private String webinarTopicIncludeRegExp;
    @Parameter(names = "--webinarTopicExcludeRegExp", description = "Exclude webinars with a topic that matches this regexp")
    private String webinarTopicExcludeRegExp;
    @Parameter(names = "--rangeStart", description = "Select webinars scheduled after this date (YYYY-MM-DD)")
    private Date rangeStart;
    @Parameter(names = "--rangeEnd", description = "Select webinars scheduled before this date (YYYY-MM-DD)")
    private Date rangeEnd;

    @Parameter(names = "--export", description = "Export selected webinars to an iCalendar file")
    private String iCalendarFileName;
    @Parameter(names = "--print", description = "Display selected webinars")
    private boolean printWebinars = false;

    @Parameter(names = "--firstName", description = "Subscriber first name")
    private String subscriberFirstName;
    @Parameter(names = "--lastName", description = "Subscribers last name")
    private String subscriberLastName;
    @Parameter(names = "--email", description = "Subscribers email address")
    private String subscriberEmail;
    @Parameter(names = "--expiryDate", description = "Subscribers expiry date (YYYY-MM-DD)")
    private Date subscriberExpiryDate;
    @Parameter(names = "--list", description = "Subscribers list (CSV file)")
    private String subscriberFileName;

    @Parameter(names = "--register", description = "Register subscribers as panelist to webinars")
    private boolean register = false;
    @Parameter(names = "--writeMail", description = "Write email messages to files ({email}.html)")
    private boolean writeMail = false;
    @Parameter(names = "--sendMail", description = "Send email messages to subscribers")
    private boolean sendMail = false;

    public boolean validateParams() throws ParameterException {
        if (!webinardIds.isEmpty() && (rangeStart != null || rangeEnd != null)) {
            throw new ParameterException("Can't use webinarId parameter with rangeStart or rangeEnd");
        }
        if (rangeStart != null && rangeEnd != null && !rangeStart.before(rangeEnd)) {
            throw new ParameterException("rangeStart parameter must be earlier than rangeEnd");
        }
        if (StringUtils.isNotEmpty(subscriberFileName) &&
            (subscriberFirstName != null || subscriberLastName != null || subscriberEmail != null || subscriberExpiryDate != null)) {
            throw new ParameterException("Can't use list parameter with firstName, lastName, email, or expiryDate");
        }
        if (!((subscriberFirstName == null && subscriberLastName == null && subscriberEmail == null) ||
              (subscriberFirstName != null && subscriberLastName != null && subscriberEmail != null))) {
            throw new ParameterException("Parameters firstName, lastName, email must be used together");
        }
        return true;
    }

    private VEvent webinarToVEvent(ZoomWebinar webinar) {
        DateTime dtStart = new DateTime(webinar.getStartTime());
        dtStart.setUtc(true);
        TemporalAmount duration = Duration.ofMinutes(webinar.getDuration());
        String summary = webinar.getTopic();
        VEvent event = new VEvent(dtStart, duration, summary);

        DateTime created = new DateTime(webinar.getCreatedAt());
        created.setUtc(true);
        event.getProperties().add(new Created(created));
        event.getProperties().add(new Uid("zoom-webinar-id-" + webinar.getId().toString()));

        String desc = Strings.nullToEmpty(appConfig.getCalendar().getDescriptionPrefix());
        if (webinar.getRegistrationUrl() != null) {
            desc += "<A HREF=" + webinar.getRegistrationUrl() + ">" + webinar.getRegistrationUrl() + "</A>\n";
        }
        desc += Strings.nullToEmpty(webinar.getAgenda());
        desc += Strings.nullToEmpty(appConfig.getCalendar().getDescriptionSuffix());
        event.getProperties().add(new Description(desc));
        return event;
    }

    public void exportWebinars(List<ZoomWebinar> webinarList, String iCalendarFileName) {
        try {
            net.fortuna.ical4j.model.Calendar iCalendar = new net.fortuna.ical4j.model.Calendar();
            iCalendar.getProperties().add(new ProdId("-//Webinar Manager CLI for Zoom//1.0//EN"));
            iCalendar.getProperties().add(CalScale.GREGORIAN);
            iCalendar.getProperties().add(Version.VERSION_2_0);
            int webinarCount = 0;

            for (ZoomWebinar webinar : webinarList) {
                VEvent event = webinarToVEvent(webinar);
                iCalendar.getComponents().add(event);
                webinarCount++;
            }
            if (webinarCount > 0) {
                try (FileOutputStream fout = new FileOutputStream(iCalendarFileName)) {
                    CalendarOutputter outputter = new CalendarOutputter();
                    outputter.output(iCalendar, fout);
                    log.info("Webinars exported to {}", iCalendarFileName);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void printWebinars(List<ZoomWebinar> webinarList) {
        try {
            if (webinarList != null) {
                for (ZoomWebinar webinar : webinarList) {
                    LocalDateTime localStartTime = webinar.getStartTime().toInstant().atZone(localZoneId).toLocalDateTime();
                    String startTime = localStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    String dayOfWeek = localStartTime.format(DateTimeFormatter.ofPattern("E"));
                    System.out.printf("%d %s %s %s\n", webinar.getId(), dayOfWeek, startTime, webinar.getTopic());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void sendMail(@NotNull InternetAddress toAddr, @NotNull String subject, @NotNull String content)  {
        try {
            MimeMessage message = new MimeMessage(mailSession);
            InternetAddress fromAddr = new InternetAddress(appConfig.getMail().getFrom().getEmail(),
                                                           appConfig.getMail().getFrom().getName(), "UTF-8");
            message.setFrom(fromAddr);

            if (appConfig.getMail().getReplyTo() != null) {
                InternetAddress[] replyToAddrs = {
                        new InternetAddress(appConfig.getMail().getReplyTo().getEmail(),
                                            appConfig.getMail().getReplyTo().getName(),
                                            "UTF-8")
                };
                message.setReplyTo(replyToAddrs);
            }

            if (appConfig.getMail().getBcc() != null) {
                InternetAddress bccAddr = new InternetAddress(appConfig.getMail().getBcc().getEmail(),
                                                              appConfig.getMail().getBcc().getName(), "UTF-8");
                message.addRecipient(Message.RecipientType.BCC, bccAddr);
            }

            message.addRecipient(Message.RecipientType.TO, toAddr);
            message.setSubject(subject,"UTF-8");

            Multipart multipart = new MimeMultipart("alternative");
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(appConfig.getMail().getTextPlainContent(), "text/plain;charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();

            messageBodyPart.setContent(content, "text/html;charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            // Specify that automatic responses are not desirable (e.g., Out of office)
            message.setHeader("Auto-Submitted", "auto-generated");
            // https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxcmail/ced68690-498a-4567-9d14-5c01f974d8b1
            message.setHeader("X-Auto-Response-Suppress", "OOF, AutoReply");
            // https://tools.ietf.org/html/rfc2369
            if (appConfig.getMail().getListUnsubscribe() != null) {
                message.setHeader("List-Unsubscribe", "<mailto:" + appConfig.getMail().getListUnsubscribe().getEmail() + "?subject=Unsubscribe>");
            }
            Transport.send(message);
            log.info("Sent message successfully to {}", toAddr.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatTimeRange(Date rangeStart, Date rangeEnd, ZoneId zoneId) {
        String rangeStr = "";
        if (rangeStart != null && rangeEnd != null) {
            LocalDateTime localRangeStart = rangeStart.toInstant().atZone(zoneId).toLocalDateTime();
            LocalDateTime localRangeEnd = rangeEnd.toInstant().atZone(zoneId).toLocalDateTime().minusDays(1L);
            int day;

            if (!localRangeStart.equals(localRangeEnd)) {
                rangeStr += ((day = localRangeStart.getDayOfMonth()) == 1) ? "1er" : String.valueOf(day);
                if (localRangeStart.getYear() != localRangeEnd.getYear())
                    rangeStr += localRangeStart.format(DateTimeFormatter.ofPattern(" MMMM yyyy"));
                else if (localRangeStart.getMonthValue() != localRangeEnd.getMonthValue())
                    rangeStr += localRangeStart.format(DateTimeFormatter.ofPattern(" MMMM"));
                rangeStr += " au ";
            }
            rangeStr += ((day = localRangeEnd.getDayOfMonth()) == 1) ? "1er" : String.valueOf(day);
            rangeStr += localRangeEnd.format(DateTimeFormatter.ofPattern(" MMMM yyyy"));
        }
        return rangeStr;
    }

    private boolean isWebinarInTimeRange(ZoomWebinar webinar, Date rangeStart, Date rangeEnd) {
        if (webinar != null) {
            Date startTime = webinar.getStartTime();
            Date endTime = Date.from(startTime.toInstant().plus(Duration.ofMinutes(webinar.getDuration())));

            if (rangeStart == null && rangeEnd == null) {
                return true;
            } else if (rangeStart != null && rangeEnd == null) {
                return rangeStart.before(endTime);
            } else if (rangeStart == null && rangeEnd != null) {
                return rangeEnd.after(startTime);
            } else {
                return rangeStart.before(endTime) && rangeEnd.after(startTime);
            }
        } else {
            return false;
        }
    }

    List<ZoomWebinar> getWebinarsByIds(List<Long> webinardIds) {
        List<ZoomWebinar> webinarList = new ArrayList<>();
        try {
            Long pageNumber = 1L;
            Long pageCount = 0L;
            int webinarCount = 0;

            do {
                log.info("Retrieving webinars (page {})", pageNumber);
                ZoomWebinars webinars = zoomClient.webinars().list().pageSize(100L).pageNumber(pageNumber).execute();
                if (webinars != null) {
                    pageCount = webinars.getPageCount();
                    for (ZoomWebinar item : webinars.getItems()) {
                        if (webinardIds.contains(item.getId())) {
                            ZoomWebinar webinar = zoomClient.webinars().get(item.getId()).execute();
                            if (webinar != null) {
                                webinarList.add(webinar);
                                webinarCount++;
                            } else {
                                log.error("Unable to retrieve webinar {}", item.getId());
                            }
                        }
                    }
                } else {
                    log.error("Unable to retrieve webinars page {}", pageNumber);
                }
            } while (pageNumber++ < pageCount);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return webinarList;
    }

    List<ZoomWebinar> getWebinarsInTimeRange(Date rangeStart, Date rangeEnd) {
        List<ZoomWebinar> webinarList = new ArrayList<>();
        try {
            Long pageNumber = 1L;
            Long pageCount = 0L;
            int webinarCount = 0;

            do {
                log.info("Retrieving webinars (page {})", pageNumber);
                ZoomWebinars webinars = zoomClient.webinars().list().pageSize(100L).pageNumber(pageNumber).execute();
                if (webinars != null) {
                    pageCount = webinars.getPageCount();
                    for (ZoomWebinar item : webinars.getItems()) {
                        if (isWebinarInTimeRange(item, rangeStart, rangeEnd)) {
                            ZoomWebinar webinar = zoomClient.webinars().get(item.getId()).execute();
                            if (webinar != null) {
                                webinarList.add(webinar);
                                webinarCount++;
                            } else {
                                log.error("Unable to retrieve webinar {}", item.getId());
                            }
                        }
                    }
                } else {
                    log.error("Unable to retrieve webinars page {}", pageNumber);
                }
            } while (pageNumber++ < pageCount);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return webinarList;
    }

    static String maskEmail(String email, String salt) {
        CRC32 crc = new CRC32();
        crc.update(email.getBytes());
        crc.update(salt.getBytes());
        return String.format("%d+%s.invalid", abs(crc.getValue()), email);
    }

    static String unmaskEmail(String maskedEmail) {
        return maskedEmail.replaceFirst("^\\d+\\+(.*)\\.invalid$","$1");
    }

    public void registerSubscribersAsPanelistToWebinars(List<Subscriber> subscribers, List<ZoomWebinar> webinarList) {
        try {
            for (ZoomWebinar webinar : webinarList) {
                List<ZoomWebinarPanelist> allPanelists = new ArrayList<>();
                LocalDateTime localStartTime = webinar.getStartTime().toInstant().atZone(localZoneId).toLocalDateTime();
                for (Subscriber s : subscribers) {
                    boolean isValid = true;
                    Date expiryDate = s.getExpiryDate();
                    if (expiryDate != null) {
                        LocalDateTime localExpiryDate = expiryDate.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
                        if (localStartTime.isAfter(localExpiryDate.plusDays(1L)))
                            isValid = false;
                    }
                     if (isValid) {
                         String subscriberEmail = appConfig.getZoom().getMaskPanelistEmail() ? maskEmail(s.getEmail(), webinar.getCreatedAt().toString()) : s.getEmail();
                         allPanelists.add(new ZoomWebinarPanelist(s.getFirstName() + " " + s.getLastName(), subscriberEmail));
                     }
                }
                List<List<ZoomWebinarPanelist>> panelistsPartitions = Lists.partition(allPanelists, 25);
                for (List<ZoomWebinarPanelist> panelistsPartition : panelistsPartitions) {
                    ZoomWebinarPanelists newPanelists = new ZoomWebinarPanelists(panelistsPartition);
                    ZoomWebinarPanelistsCreateResponse response = zoomClient.webinarPanelists().create(webinar.getId(), newPanelists).execute();
                    log.info("Registered {} subscriber(s) for webinar {}", panelistsPartition.size(), response.getId());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // Return HashMap: <WebinarId,Email> => JoinUrl
    public Map<Pair<Long,String>,String> getSubscribersJoinUrls(List<ZoomWebinar> webinarList) {
        Map<Pair<Long,String>,String> panelistsJoinUrlMap = new HashMap<>();
        try {
            for (ZoomWebinar webinar : webinarList) {
                ZoomWebinarPanelists panelists = zoomClient.webinarPanelists().list(webinar.getId()).execute();
                for (ZoomWebinarPanelist panelist : panelists.getItems()) {
                    String panelistEmail = appConfig.getZoom().getMaskPanelistEmail() ? unmaskEmail(panelist.getEmail()) : panelist.getEmail();
                    panelistsJoinUrlMap.put(new ImmutablePair<>(webinar.getId(), panelistEmail), panelist.getJoinUrl());
                }
                log.info("Downloaded {} panelists for webinar {}", panelists.getTotalRecords(), webinar.getId());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return panelistsJoinUrlMap;
    }

    public void processSubscriberEmails(List<Subscriber> subscribers, List<ZoomWebinar> webinarList, Map<Pair<Long,String>,String> panelistsJoinUrlMap) {
        try {
            String template = appConfig.getMail().getTextHtmlContent();
            String subject = appConfig.getMail().getSubject().replaceFirst("\\{TIMERANGE}", formatTimeRange(rangeStart, rangeEnd, localZoneId));
            template = template.replaceFirst("<!--SUBJECT-->", subject);
            LocalDateTime localNow = LocalDateTime.now(localZoneId);

            for (Subscriber subscriber: subscribers) {
                log.info("Subscriber: {} {} <{}>", subscriber.getFirstName(), subscriber.getLastName(), subscriber.getEmail());
                String content = template.replaceFirst("<!--FIRSTNAME-->", subscriber.getFirstName());
                Date expiryDate = subscriber.getExpiryDate();
                LocalDateTime localExpiryDate = null;

                if (expiryDate != null) {
                    localExpiryDate = expiryDate.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
                    long expiryInDays = Duration.between(localNow, localExpiryDate).toDays();
                    if (expiryInDays <= appConfig.getMail().getRenewalNoticePeriodInDays()) {
                        int day;
                        String renewalNotice = appConfig.getMail().getRenewalNotice();
                        String expiryDateStr = ((day = localExpiryDate.getDayOfMonth()) == 1) ? "1er" : String.valueOf(day);
                        expiryDateStr += localExpiryDate.format(DateTimeFormatter.ofPattern(" MMMM yyyy"));
                        renewalNotice = renewalNotice.replaceFirst("<!--EXPIRYDATE-->", expiryDateStr);
                        content = content.replaceFirst("<!--NOTICE-->", renewalNotice);
                        expiryDateStr = localExpiryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        log.info("Expiry Date: {} ({} days left)", expiryDateStr, expiryInDays);
                    }
                }

                StringBuilder sb = new StringBuilder();
                int webinarCount = 0;
                boolean expiryBannerPrinted = false;

                for (ZoomWebinar webinar : webinarList) {
                    Date utcStartTime = webinar.getStartTime();
                    LocalDateTime localStartTime = utcStartTime.toInstant().atZone(localZoneId).toLocalDateTime();
                    String tmpStartDate = localStartTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy"));
                    String startDate = tmpStartDate.substring(0, 1).toUpperCase() + tmpStartDate.substring(1);
                    String startTime = localStartTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    String url;

                    if (localExpiryDate != null && localStartTime.isAfter(localExpiryDate.plusDays(1L))) {
                        if (!expiryBannerPrinted) {
                            if (StringUtils.isNotEmpty(appConfig.getMail().getExpiryTableRow())) {
                                sb.append(appConfig.getMail().getExpiryTableRow());
                            }
                            expiryBannerPrinted = true;
                        }
                        url = webinar.getRegistrationUrl();
                    } else {
                        url = panelistsJoinUrlMap.get(new ImmutablePair<>(webinar.getId(), subscriber.getEmail()));
                        webinarCount++;
                    }
                    sb.append(String.format("<tr><td>%s</td><td>%s</td><td><a href=\"%s\">%s</a></td></tr>%n", startDate, startTime, url, webinar.getTopic()));
                }
                content = content.replaceFirst("<!--TABLEROWS-->", sb.toString());

                if (writeMail) {
                    try (PrintWriter out = new PrintWriter(subscriber.getEmail() + ".html")) {
                        out.print(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (sendMail && webinarCount > 0) {
                    InternetAddress toAddr = new InternetAddress(subscriber.getEmail(), subscriber.getFirstName() + " " + subscriber.getLastName(), "UTF-8");
                    sendMail(toAddr, subject, content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Subscriber> getSubscribers(String csvFileName) {
        List<Subscriber> subscribers = new ArrayList<>();

        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            ObjectReader oReader = csvMapper.readerFor(Subscriber.class).with(schema);

            try (Reader reader = new FileReader(csvFileName)) {
                MappingIterator<Subscriber> mi = oReader.readValues(reader);
                while (mi.hasNext()) {
                    Subscriber current = mi.next();
                    subscribers.add(current);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subscribers;
    }

    private void initialize() throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.findAndRegisterModules();

            File configFile;
            if (StringUtils.isNotEmpty(configFileName)) {
                configFile = new File(configFileName);
            } else {
                String homeDir = System.getProperty("user.home");
                configFileName = homeDir + File.separator + ".webinar-cli" + File.separator + "config.yml";
                configFile = new File(configFileName);
                if (!configFile.exists()) {
                    configFileName = "config.yml";
                    configFile = new File(configFileName);
                }
            }
            log.info("Configuration file: {}", configFileName);

            appConfig = mapper.readValue(configFile, AppConfiguration.class);

            zoomClient = ZoomClient.builder().fromConfig(appConfig.getZoom()).build();

            Properties properties = System.getProperties();
            properties.put("mail.smtp.host",       appConfig.getMail().getSmtpHost());
            properties.put("mail.smtp.port",       appConfig.getMail().getSmtpPort());
            properties.put("mail.smtp.auth",       appConfig.getMail().getSmtpAuth());
            properties.put("mail.smtp.ssl.enable", appConfig.getMail().getSmtpSslEnable());

            String from = appConfig.getMail().getFrom().getEmail();
            String fromPasswd = appConfig.getMail().getFromPassword();
            Authenticator authenticator = new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, fromPasswd);
                }
            };

            mailSession = Session.getInstance(properties, authenticator);
            mailSession.setDebug(appConfig.getMail().getDebug());
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            List<ZoomWebinar> webinarList;
            if (!webinardIds.isEmpty()) {
                webinarList = getWebinarsByIds(webinardIds);
            } else {
                if (rangeStart == null) {
                    rangeStart = new Date();
                }
                webinarList = getWebinarsInTimeRange(rangeStart, rangeEnd);
                if (StringUtils.isNotEmpty(webinarTopicIncludeRegExp)) {
                    webinarList.removeIf(webinar -> !Pattern.compile(webinarTopicIncludeRegExp).matcher(webinar.getTopic()).matches());
                }
                if (StringUtils.isNotEmpty(webinarTopicExcludeRegExp)) {
                    webinarList.removeIf(webinar -> Pattern.compile(webinarTopicExcludeRegExp).matcher(webinar.getTopic()).matches());
                }
            }

            if (webinarList != null && webinarList.size() > 0) {
                Collections.sort(webinarList);
                rangeStart = webinarList.get(0).getStartTime();
                Date rangeEndDate = webinarList.get(webinarList.size() - 1).getStartTime();
                LocalDateTime rangeEndLocalDateTime = rangeEndDate.toInstant().atZone(localZoneId).toLocalDateTime().plusDays(1L);
                rangeEnd = Date.from(rangeEndLocalDateTime.atZone(localZoneId).toInstant());
                log.info("Webinars in time range ({}) = {}", formatTimeRange(rangeStart, rangeEnd, localZoneId), webinarList.size());

                if (printWebinars) {
                    printWebinars(webinarList);
                }
                if (StringUtils.isNotEmpty(iCalendarFileName)) {
                    exportWebinars(webinarList, iCalendarFileName);
                }

                List<Subscriber> subscribers = null;
                if (StringUtils.isNotEmpty(subscriberFileName)) {
                    subscribers = getSubscribers(subscriberFileName);
                } else if (StringUtils.isNotEmpty(subscriberFirstName) && StringUtils.isNotEmpty(subscriberLastName) && StringUtils.isNotEmpty(subscriberEmail)) {
                    Subscriber subscriber = Subscriber.builder()
                            .firstName(subscriberFirstName)
                            .lastName(subscriberLastName)
                            .email(subscriberEmail)
                            .expiryDate(subscriberExpiryDate)
                            .build();
                    subscribers = Collections.singletonList(subscriber);
                }

                if (subscribers != null && !subscribers.isEmpty()) {
                    if (register) {
                        registerSubscribersAsPanelistToWebinars(subscribers, webinarList);
                    }
                    if (writeMail || sendMail) {
                        Map<Pair<Long, String>, String> panelistsJoinUrlMap = getSubscribersJoinUrls(webinarList);
                        processSubscriberEmails(subscribers, webinarList, panelistsJoinUrlMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JCommander jc = null;
        try {
            log.info("Webinar Manager CLI for Zoom - Version 1.0");
            App main = new App();
            jc = JCommander.newBuilder().addObject(main).build();
            jc.parse(args);
            if (main.validateParams()) {
                main.initialize();
                main.run();
            }
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            if (jc != null)
                jc.usage();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
