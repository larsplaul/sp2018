package rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author plaul1
 */
@Path("status")
public class TestResource {
    public static String STATUS = "DEBUG";
  
    @Context
    private UriInfo context;

 
    /**
     * Info whether we are running in debug mode
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getStatus() {
      return String.format("{\"status\" : \"%s\"}",STATUS);
    }

   
}
