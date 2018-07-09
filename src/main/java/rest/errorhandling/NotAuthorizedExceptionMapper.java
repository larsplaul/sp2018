package rest.errorhandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Context
  ServletContext context;

  @Override
  public Response toResponse(NotAuthorizedException ex) {
    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");
    ErrorMessage err = new ErrorMessage(ex, ex.getResponse().getStatus(), isDebug);
    try{
      //Note: This system kind of "misuses" the NotAuthorizedException as a general autentication exception. So challenges does not contain a challenge
      //But the message to display. Some new Exception classes should probably be introduces
      String msg2 = ex.getMessage();
      System.out.println(msg2);
      String msg = ex.getChallenges().get(0).toString();
      err.setMessage(msg);
    }
    catch(Exception e){
      System.out.println("UPS");
      //Do nothing. If null pointer exception just use defaults
    }
    err.setDescription("You could either not be authenticated, or are not authorized to perform this request");
    
    return Response.status(ex.getResponse().getStatus())
            .entity("{\"error\":"+gson.toJson(err)+"}")
            .type(MediaType.APPLICATION_JSON).
            build();
  }
}
