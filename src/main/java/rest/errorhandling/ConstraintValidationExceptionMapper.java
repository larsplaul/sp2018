package rest.errorhandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.exceptions.NonexistentEntityException;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Context
  ServletContext context;

  @Override
  public Response toResponse(javax.validation.ConstraintViolationException ex) {

    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");
    ErrorMessage err = new ErrorMessage(ex, 400, isDebug);
    err.setMessage("Validation Error");
    err.setDescription("");
    boolean first = true;
    for (ConstraintViolation v : ex.getConstraintViolations()) {
      String e = err.getDescription(); 
      if (!first) {
        e = ", " + v.getPropertyPath() + " : " + v.getMessage();
      } else {
        e = v.getPropertyPath() + " : " + v.getMessage();
        first = false;
      }
      err.setDescription(e);
    }

    //  err.setDescription("An attemt was made to locate an item in the database which did not exist");
    return Response.status(400)
            .entity(gson.toJson(err))
            .entity("{\"error\":"+gson.toJson(err)+"}")
            .build();
  }
}
