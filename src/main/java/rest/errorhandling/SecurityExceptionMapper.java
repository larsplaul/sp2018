package rest.errorhandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.exceptions.NonexistentEntityException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Context
  ServletContext context;

  @Override
  public Response toResponse(SecurityException ex) {

    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");
    ErrorMessage err = new ErrorMessage(ex, 400, isDebug);
    //err.setMessage("An unexpected problem occured on the server");
    err.setDescription("A security related problem occured");
    return Response.status(400)
            .entity("{\"error\":"+gson.toJson(err)+"}")
            .type(MediaType.APPLICATION_JSON).
            build();
  
  }
}
