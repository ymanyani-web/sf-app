package it.agrimontana.salesforce.monitoring;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/monitoring/api")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource {

    @Inject
    DashboardDAO dao;

    @GET
    @Path("/kpi")
    public Response kpi(@QueryParam("hours") @DefaultValue("24") int hours) {
        try {
            return Response.ok(dao.kpiLastHours(hours)).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("status","KO","message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/trend")
    public Response trend(@QueryParam("minutes") @DefaultValue("60") int minutes) {
        try {
            List<Map<String, Object>> rows = dao.hitsPerMinuteLastMinutes(minutes);
            return Response.ok(rows).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("status","KO","message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/latest")
    public Response latest(@QueryParam("limit") @DefaultValue("50") int limit) {
        try {
            return Response.ok(dao.latestRequests(limit)).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("status","KO","message", e.getMessage())).build();
        }
    }
}