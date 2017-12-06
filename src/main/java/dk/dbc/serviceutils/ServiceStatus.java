package dk.dbc.serviceutils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
}
