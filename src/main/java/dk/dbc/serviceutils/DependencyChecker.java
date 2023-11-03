/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt
 */

package dk.dbc.serviceutils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import dk.dbc.httpclient.FailSafeHttpClient;
import dk.dbc.httpclient.HttpClient;
import dk.dbc.httpclient.HttpGet;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import net.jodah.failsafe.RetryPolicy;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DependencyChecker {
    private static final Set<Integer> RETRY_CODES = Set.of(500, 502);
    private static final RetryPolicy<Response> RETRY_POLICY = new RetryPolicy<Response>()
            .handle(ProcessingException.class)
            .handleResultIf(r -> RETRY_CODES.contains(r.getStatus()))
            .withDelay(Duration.ofSeconds(3))
            .withMaxRetries(3);

    public static List<HowRU.Upstream> checkUpstreams(List<String> urls) {
        final List<HowRU.Upstream> upstreams = new ArrayList<>();
        final Client client = HttpClient.newClient();
        try {
            final FailSafeHttpClient failSafeHttpClient = FailSafeHttpClient.create(client, RETRY_POLICY);
            for (String url : urls) {
                try(Response response = new HttpGet(failSafeHttpClient).withBaseUrl(url).execute()) {
                    HowRU.Upstream upstream = new HowRU.Upstream().withEndpoint(url).withStatus(response.getStatus());
                    upstreams.add(upstream);
                }
            }
            return upstreams;
        } finally {
            client.close();
        }
    }

    public static List<String> parseUpstreamsJsonList(String upstreamsJsonList)
            throws IOException {
        /* use JsonParser instead of JSONBContext because i encountered
         * this error when trying to construct a JavaType for List<String>:
         * java.lang.LinkageError: loader constraint violation: loader (instance of org/glassfish/web/loader/WebappClassLoader)
         * previously initiated loading for a different type with name "com/fasterxml/jackson/databind/type/TypeFactory"
         */
        final JsonFactory jsonFactory = new JsonFactory();
        final JsonParser parser = jsonFactory.createParser(
                upstreamsJsonList);
        final List<String> upstreamUrls = new ArrayList<>();
        while (!parser.isClosed()) {
            final JsonToken token = parser.nextToken();
            if (JsonToken.START_ARRAY.equals(token)) {
                while (!JsonToken.END_ARRAY.equals(parser.nextToken())) {
                    upstreamUrls.add(parser.getText());
                }
            }
        }
        return upstreamUrls;
    }
}
