package rest.errorhandling;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.naming.AuthenticationException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;



@Provider
public class AuthenticationExceptionMapper  implements ExceptionMapper<AuthenticationException> {

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Context
  ServletContext context;

  @Override
  public Response toResponse(AuthenticationException ex) {

    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");
    ErrorMessage err = new ErrorMessage(ex, 403, isDebug);
    err.setDescription("You could not be authenticated");
    
    return Response.status(403)
            .entity("{\"error\":"+gson.toJson(err)+"}")
            .type(MediaType.APPLICATION_JSON).
            build();
  }
}

