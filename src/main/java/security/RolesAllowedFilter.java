package security;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RolesAllowedFilter implements ContainerRequestFilter {

//  private static final Response NOT_FOUND = Response.status(
//          Response.Status.NOT_FOUND).entity("{\"message\": \"Resource Not Found\"}").build();

  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    Method resourceMethod = resourceInfo.getResourceMethod();

    // DenyAll on the method take precedence over RolesAllowed and PermitAll
    if (resourceMethod.isAnnotationPresent(DenyAll.class)) {
      
      throw new NotAuthorizedException("Resource Not Found",Response.Status.NOT_FOUND);
      //return;
    }

    // RolesAllowed on the method takes precedence over PermitAll
    RolesAllowed ra = resourceMethod.getAnnotation(RolesAllowed.class);
    if (assertRole(requestContext, ra)) {
      return;
    }

    // PermitAll takes precedence over RolesAllowed on the class
    if (resourceMethod.isAnnotationPresent(PermitAll.class)) {
      // Do nothing.
      return;
    }

    if (resourceInfo.getResourceClass().isAnnotationPresent(DenyAll.class)) {
      //requestContext.abortWith(NOT_FOUND);
      throw new NotAuthorizedException("Resource Not Found",Response.Status.NOT_FOUND);
    }

    // RolesAllowed on the class takes precedence over PermitAll
    ra = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
    if (assertRole(requestContext, ra)) {
      return;
    }
  }

  private boolean assertRole(ContainerRequestContext requestContext, RolesAllowed ra) {

    if (ra != null) {
      String[] roles = ra.value();
      for (String role : roles) {
        if (requestContext.getSecurityContext().isUserInRole(role)) {
          return true;
        }
      }
      throw new NotAuthorizedException("You are not authorized to perform the requested operation",Response.Status.UNAUTHORIZED);
    }
    return false;
  }

//  private void abort(ContainerRequestContext requestContext) {
//    JsonObject responseJson = new JsonObject();
//    responseJson.addProperty("error", "Unauthorized");
//    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity(new Gson().toJson(responseJson)).build());
//  }
}
