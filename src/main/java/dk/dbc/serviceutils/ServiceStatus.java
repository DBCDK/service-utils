package dk.dbc.serviceutils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public interface ServiceStatus {
    String OK_ENTITY = new HowRU().withStatus(200).toJson();

    @GET
    @Path("status")
    @Produces({MediaType.APPLICATION_JSON})
    default Response getStatus() {
        return Response.ok().entity(OK_ENTITY).build();
    }

    @GET
    @Path("howru")
    @Produces({MediaType.APPLICATION_JSON})
    default Response howru() {
        try {
            return ServiceStatus.this.getStatus();
        } catch (Exception e) {
            HowRU.Error error = new HowRU.Error()
                .fromThrowable(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new HowRU().withError(error).toJson())
                .build();
        }
    }

    /**
     * check upstreams based on a list of upstream urls
     * @param upstreams list of strings containing upstream urls
     * @return 200 OK with json structure describing application and
     * upstreams health
     */
    default Response howru(List<String> upstreams) {
        try {
            final Response response = ServiceStatus.this.getStatus();
            final HowRU howRU = new HowRU()
                .withStatus(response.getStatus())
                .withUpstreams(DependencyChecker.checkUpstreams(upstreams));
            return Response.ok(howRU.toJson()).build();
        } catch (Exception e) {
            HowRU.Error error = new HowRU.Error()
                .fromThrowable(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new HowRU().withError(error).toJson())
                .build();
        }
    }

    /**
     * check upstreams based on a string containing a json list
     * @param upstreamsJsonList string containing a json list with upstreams
     * @return 200 OK with json structure describing application and
     *         500 Internal Server Error on error parsing json
     * upstreams health
     */
    default Response howru(String upstreamsJsonList) {
        try {
            List<String> upstreamUrls = DependencyChecker
                .parseUpstreamsJsonList(upstreamsJsonList);
            return howru(upstreamUrls);
        } catch (IOException e) {
            final HowRU.Error error = new HowRU.Error().fromThrowable(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new HowRU().withError(error)).build();
        }
    }
}
