/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entity.exceptions.NonexistentEntityException;
import entity.exceptions.PreexistingEntityException;
import facade.JsonAssembler;
import facade.LogFacade;
import facade.LogMessage;
import javax.annotation.security.RolesAllowed;
import javax.naming.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import security.PasswordStorage;

@Path("userDetails")
@RolesAllowed({"User", "Admin"})
public class AllRoles {

  @Context
  SecurityContext securityContext;

  private final JsonAssembler jsonAssembler;

  public AllRoles() {
    jsonAssembler = new JsonAssembler();
  }

  @GET
  @Produces("application/json")
  public String getClassesForCurrentUser() {
    String user = securityContext.getUserPrincipal().getName();
    return jsonAssembler.getUser(user);
  }
  
  @PUT
  @Produces("application/json")
  @Path("/edit")
  public String editUser(String userToEdit) throws PreexistingEntityException, NonexistentEntityException {
     String user = securityContext.getUserPrincipal().getName();
     return jsonAssembler.editUser(userToEdit, user);        
  }
  
  @PUT
  @Produces("application/json")
  @Path("/changePassword")
  public String changePassword(String passwordInfo) throws PreexistingEntityException, NonexistentEntityException, PasswordStorage.CannotPerformOperationException, PasswordStorage.InvalidHashException, AuthenticationException {
     String user = securityContext.getUserPrincipal().getName();
     LogFacade.addLogEntry(user, LogMessage.changePassword);
     return jsonAssembler.editPassword(passwordInfo, user);        
  }
  
  @Path("myStudyPoints/{classId}")
  @GET
  @Produces("application/json")
  public Response studypointsForStudentClass(@PathParam("classId") String classId){
    String user = securityContext.getUserPrincipal().getName();
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getStudyPointsForCurrentUser(classId, user,false))
            .build();
  }

}