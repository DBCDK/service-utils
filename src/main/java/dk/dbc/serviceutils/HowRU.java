package dk.dbc.serviceutils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dk.dbc.commons.jsonb.JSONBContext;
import dk.dbc.commons.jsonb.JSONBException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class HowRU {
    public static final JSONBContext jsonbContext = new JSONBContext();
    private String version;
    private int status;
    private List<Upstream> upstreams;
    private Error error;

    public String getVersion() {
        return version;
    }

    public HowRU withVersion(String version) {
        this.version = version;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public HowRU withStatus(int status) {
        this.status = status;
        return this;
    }

    public List<Upstream> getUpstreams() {
        return upstreams;
    }

    public HowRU withUpstreams(List<Upstream> upstreams) {
        this.upstreams = upstreams;
        return this;
    }

    public Error getError() {
        return error;
    }

    public HowRU withError(Error error) {
        this.error = error;
        return this;
    }

    public String toJson() {
        try {
            return jsonbContext.marshall(this);
        } catch(JSONBException e) {
            throw new IllegalStateException(e);
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Upstream {
        private String endpoint;
        private int status;

        public String getEndpoint() {
            return endpoint;
        }

        public Upstream withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public int getStatus() {
            return status;
        }

        public Upstream withStatus(int status) {
            this.status = status;
            return this;
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Error {
        private String message;
        private String details;

        public String getMessage() {
            return message;
        }

        public Error withMessage(String message) {
            this.message = message;
            return this;
        }

        public String getDetails() {
            return details;
        }

        public Error withDetails(String details) {
            this.details = details;
            return this;
        }

        public Error fromThrowable(Throwable throwable) {
            StringWriter sw = new StringWriter();
            try(PrintWriter pw = new PrintWriter(sw)) {
                throwable.printStackTrace(pw);
                this.details = sw.toString();
                this.message = throwable.getMessage();
                return this;
            }
        }
    }
}
