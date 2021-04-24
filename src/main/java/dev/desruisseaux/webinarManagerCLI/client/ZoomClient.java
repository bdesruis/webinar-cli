package dev.desruisseaux.webinarManagerCLI.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.desruisseaux.webinarManagerCLI.ZoomConfiguration;
import dev.desruisseaux.webinarManagerCLI.api.ZoomContacts;
import dev.desruisseaux.webinarManagerCLI.api.ZoomUser;
import dev.desruisseaux.webinarManagerCLI.api.ZoomUsers;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinar;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarPanelist;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarPanelists;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarPanelistsCreateResponse;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarRegistrant;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinarRegistrants;
import dev.desruisseaux.webinarManagerCLI.api.ZoomWebinars;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class ZoomClient {
    private final static ThreadLocal<ObjectMapper> mapperLocal = new ThreadLocal<>();
    private final String apiBaseUrl;
    private final String jwtToken;

    public static synchronized ObjectMapper getMapper() {
        ObjectMapper mapper = mapperLocal.get();
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapperLocal.set(mapper);
        }
        return mapper;
    }

    private ZoomClient(String apiBaseUrl,
                       String jwtToken) {
        this.apiBaseUrl = apiBaseUrl;
        this.jwtToken = jwtToken;
    }

    public static ZoomClientBuilder builder() {
        return new ZoomClientBuilder();
    }

    public ZoomClient.Webinars webinars() {
        return new ZoomClient.Webinars();
    }

    public ZoomClient.WebinarPanelists webinarPanelists() {
        return new ZoomClient.WebinarPanelists();
    }

    public ZoomClient.WebinarRegistrants webinarRegistrants() {
        return new ZoomClient.WebinarRegistrants();
    }

    public ZoomClient.Users users() { return new ZoomClient.Users(); }

    public ZoomClient.Contacts contacts() { return new ZoomClient.Contacts(); }

    public static class ZoomClientBuilder {
        private static final String defaultApiBaseUrl = "https://api.zoom.us/v2";
        private String apiBaseUrl;
        private boolean apiBaseUrl$set;
        private String jwtToken;

        ZoomClientBuilder() {
        }

        public ZoomClientBuilder fromConfig(ZoomConfiguration config) {
            if (StringUtils.isNotEmpty(config.getApiBaseUrl())) {
                this.apiBaseUrl = config.getApiBaseUrl();
                this.apiBaseUrl$set = true;
            }
            this.jwtToken = config.getJwtToken();
            return this;
        }

        public ZoomClientBuilder apiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            this.apiBaseUrl$set = true;
            return this;
        }

        public ZoomClientBuilder jwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
            return this;
        }

        public ZoomClient build() {
            return new ZoomClient(apiBaseUrl$set ? apiBaseUrl : defaultApiBaseUrl, jwtToken);
        }
    }

    public class Webinars {
        public Webinars() {
        }

        public ZoomClient.Webinars.Get get(Long id) {
            return new ZoomClient.Webinars.Get(id);
        }

        public class Get {
            private final Long id;

            public Get(Long id) {
                this.id = id;
            }

            public ZoomWebinar execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/webinars/" + id;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomWebinar.class);
            }
        }

        public ZoomClient.Webinars.List list() {
            return new ZoomClient.Webinars.List();
        }

        public class List {
            private Long pageSize = 30L;
            private Long pageNumber = 1L;
            private String userId = "me"; // User-level app can pass "me" as the userId

            public List() {
            }

            public ZoomClient.Webinars.List pageSize(Long pageSize) {
                this.pageSize = pageSize;
                return this;
            }

            public ZoomClient.Webinars.List pageNumber(Long pageNumber) {
                this.pageNumber = pageNumber;
                return this;
            }
            public ZoomClient.Webinars.List userId(String userId) {
                this.userId = userId;
                return this;
            }

            public ZoomWebinars execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/users/" + userId + "/webinars?page_size=" + pageSize + "&page_number=" + pageNumber;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomWebinars.class);
            }
        }
    }

    public class WebinarPanelists {
        public WebinarPanelists() {
        }

        public ZoomClient.WebinarPanelists.Get get(Long webinarId, Long panelistId) {
            return new ZoomClient.WebinarPanelists.Get(webinarId, panelistId);
        }

        public class Get {
            private final Long webinarId;
            private final Long panelistId;

            public Get(Long webinarId, Long panelistId) {
                this.webinarId = webinarId;
                this.panelistId = panelistId;
            }

            public ZoomWebinarPanelist execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/webinars/" + webinarId + "/panelists/" + panelistId;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomWebinarPanelist.class);
            }
        }

        public ZoomClient.WebinarPanelists.Create create(Long webinarId, ZoomWebinarPanelists panelists) {
            return new ZoomClient.WebinarPanelists.Create(webinarId, panelists);
        }

        public class Create {
            private final Long webinarId;
            private final ZoomWebinarPanelists panelists;

            public Create(Long webinarId, ZoomWebinarPanelists panelists) {
                this.webinarId = webinarId;
                this.panelists = panelists;
            }

            public ZoomWebinarPanelistsCreateResponse execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/webinars/" + webinarId + "/panelists/";
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("Accept", "*/*");
                OutputStream os = conn.getOutputStream();
                getMapper().writeValue(os, panelists);
                InputStream is = conn.getInputStream();
                return getMapper().readValue(is, ZoomWebinarPanelistsCreateResponse.class);
            }
        }

        public ZoomClient.WebinarPanelists.List list(Long webinarId) {
            return new ZoomClient.WebinarPanelists.List(webinarId);
        }

        public class List {
            private Long webinarId;

            public List(Long webinarId) {
                this.webinarId = webinarId;
            }

            public ZoomWebinarPanelists execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/webinars/" + webinarId + "/panelists";
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomWebinarPanelists.class);
            }
        }
    }

    public class WebinarRegistrants {
        public WebinarRegistrants() {
        }

        public ZoomClient.WebinarRegistrants.Get get(Long webinarId, Long registrantId) {
            return new ZoomClient.WebinarRegistrants.Get(webinarId, registrantId);
        }

        public class Get {
            private final Long webinarId;
            private final Long registrantId;

            public Get(Long webinarId, Long registrantId) {
                this.webinarId = webinarId;
                this.registrantId = registrantId;
            }

            public ZoomWebinarRegistrant execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/webinars/" + webinarId + "/registrants/" + registrantId;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomWebinarRegistrant.class);
            }
        }

        public ZoomClient.WebinarRegistrants.List list(Long webinarId) {
            return new ZoomClient.WebinarRegistrants.List(webinarId);
        }

        public class List {
            private Long webinarId;

            public List(Long webinarId) {
                this.webinarId = webinarId;
            }

            public ZoomWebinarRegistrants execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/webinars/" + webinarId + "/registrants";
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomWebinarRegistrants.class);
            }
        }
    }

    public class Users {
        public Users() {
        }

        public ZoomClient.Users.Get get(String id) {
            return new ZoomClient.Users.Get(id);
        }

        public class Get {
            private final String id;

            public Get(String id) {
                this.id = id;
            }

            public ZoomUser execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/users/" + id;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomUser.class);
            }
        }

        public ZoomClient.Users.List list() {
            return new ZoomClient.Users.List();
        }

        public class List {
            private String status = "active";
            private Long pageSize = 30L;
            private Long pageNumber = 1L;

            public List() {
            }

            public ZoomClient.Users.List status(String status) {
                this.status = status;
                return this;
            }

            public ZoomClient.Users.List pageSize(Long pageSize) {
                this.pageSize = pageSize;
                return this;
            }

            public ZoomClient.Users.List pageNumber(Long pageNumber) {
                this.pageNumber = pageNumber;
                return this;
            }

            public ZoomUsers execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/users?status=" + status + "&page_size=" + pageSize + "&page_number=" + pageNumber;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomUsers.class);
            }
        }
    }

    public class Contacts {
        public Contacts() {
        }

        public ZoomClient.Contacts.Get get(String id) {
            return new ZoomClient.Contacts.Get(id);
        }

        public class Get {
            private final String id;

            public Get(String id) {
                this.id = id;
            }

            public ZoomUser execute() throws IOException {
                String url = ZoomClient.this.apiBaseUrl + "/contacts/" + id;
                return ZoomClient.execute(url, ZoomClient.this.jwtToken, ZoomUser.class);
            }
        }
        public ZoomClient.Contacts.List list() {
            return new ZoomClient.Contacts.List();
        }

        public class List {
            private String searchKey;
            private String queryPresenceStatus;
            private Long pageSize = 25L;
            private String nextPageToken;

            public List() {
            }

            public ZoomClient.Contacts.List searchKey(String searchKey) {
                this.searchKey = searchKey;
                return this;
            }

            public ZoomClient.Contacts.List queryPresenceStatus(String queryPresenceStatus) {
                this.queryPresenceStatus = queryPresenceStatus;
                return this;
            }

            public ZoomClient.Contacts.List pageSize(Long pageSize) {
                this.pageSize = pageSize;
                return this;
            }

            public ZoomClient.Contacts.List nextPageToken(String nextPageToken) {
                this.nextPageToken = nextPageToken;
                return this;
            }

            public ZoomContacts execute() throws IOException, URISyntaxException {
                URIBuilder url = new URIBuilder(ZoomClient.this.apiBaseUrl + "/contacts");
                url.addParameter("page_size", String.valueOf(pageSize));
                if (StringUtils.isNotEmpty(searchKey))
                    url.addParameter("search_key", searchKey);
                if (StringUtils.isNotEmpty(queryPresenceStatus))
                    url.addParameter("query_presence_status", queryPresenceStatus);
                if (StringUtils.isNotEmpty(nextPageToken))
                    url.addParameter("next_page_token", nextPageToken);

                return ZoomClient.execute(url.toString(), ZoomClient.this.jwtToken, ZoomContacts.class);
            }
        }
    }

    // TODO: Configure request filter to add credentials automatically?
    public static <T> T execute(String url, String jwtToken, Class<T> valueType) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        InputStream is = conn.getInputStream();
        return getMapper().readValue(is, valueType);
    }
}