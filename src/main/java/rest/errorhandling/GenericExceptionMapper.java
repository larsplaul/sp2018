package rest.errorhandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Context
  ServletContext context;

  @Override
  public Response toResponse(Throwable ex) {

    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");
    ErrorMessage err = new ErrorMessage(ex, 500, isDebug);
    //For most unknow exceptions we hide the original text, in order not to expose internal logic
    // Some messages however, are VERY nice to have for our self
    if(!(ex instanceof javax.ws.rs.NotAllowedException)){
      err.setMessage("An unexpected problem occured on the server");
      err.setDescription("You may report this error to lam@cphbusiness.dk with a description off how the error occured ");
    }
    
   
    return Response.status(500)
            .entity("{\"error\":"+gson.toJson(err)+"}")
            .type(MediaType.APPLICATION_JSON).
            build();
  }
}
