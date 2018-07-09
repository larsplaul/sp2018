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
public class NonexistentEntityExceptionMapper implements ExceptionMapper<NonexistentEntityException> {

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Context
  ServletContext context;

  @Override
  public Response toResponse(NonexistentEntityException ex) {

    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");
    ErrorMessage err = new ErrorMessage(ex, 400, isDebug);
    err.setDescription("An attemt was made to locate an item in the database which did not exist");
    return Response.status(400)
             .entity("{\"error\":"+gson.toJson(err)+"}")
            .type(MediaType.APPLICATION_JSON).
            build();
  }
}

 