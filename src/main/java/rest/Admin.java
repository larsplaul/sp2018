
package rest;

import entity.exceptions.NonexistentEntityException;
import entity.exceptions.PreexistingEntityException;
import entity.exceptions.StudyPointException;
import facade.JsonAssembler;
import facade.LogFacade;
import facade.LogMessage;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import security.PasswordStorage;

@Path("admin")
@RolesAllowed("Admin") 
public class Admin {

  @Context
  private UriInfo context;
  @Context
  SecurityContext securityContext;
  
  private final JsonAssembler jsonAssembler;
 
  public Admin() {
    jsonAssembler = new JsonAssembler();
  }


  
  @Path("classes")
  @GET
  @Produces("application/json")
  public Response getClasses() {
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getAllClasses())
            .build();
  }
  
  @Path("users")
  @GET
  @Produces("application/json")
  public Response getUsers() {
    
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getAllUsers())
            .build();
  }
  
  @Path("classesFullInfo/{id}")
  @GET
  @Produces("application/json")
  public Response getClassesFullInfo(@PathParam("id") String id) {
     return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getFullClassInfo(id))
            .build();
  }
  
  
  @Path("classFromId/{id}")
  @GET
  @Produces("application/json")
  public Response getClassJson(@PathParam("id") String id){
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getClass(id))
            .build();
  }
  @Path("classesForStudent/{id}")
  @GET
  @Produces("application/json")
  public Response getClassesForStudent(@PathParam("id") int id){
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getClassesForStudent(id))
            .build();
  }

  @Path("period/{id}")
  @GET
  @Produces("application/json")
  public Response getPeriodJson(@PathParam("id") int id){
     return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getPeriod(id))
            .build();
  }
  
  @Path("studypointsForStudentClass/{classId}/{studentId}")
  @GET
  @Produces("application/json")
  public Response studypointsForStudentClass(@PathParam("classId") String classId,@PathParam("studentId") int studentId){
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getStudyPointsForStudent(classId, studentId,false))
            .build();
  }
  
  @RolesAllowed("Super") 
  @Path("addEditUser")
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  public Response makeNewUser(String data) throws PreexistingEntityException{
    String result = jsonAssembler.makeUser(data);
    
    String user = securityContext.getUserPrincipal().getName();
    LogFacade.addLogEntry(user, LogMessage.newUser,result);
    
    return Response
            .status(200)
            .entity(result)
            .build();
  }
  
  @RolesAllowed("Super") 
  @Path("addEditUser")
  @PUT
  @Produces("application/json")
  @Consumes("application/json")
  public Response editUser(String data) throws PreexistingEntityException, NonexistentEntityException{
    String result = jsonAssembler.editUser(data,null);
    String user = securityContext.getUserPrincipal().getName();
    LogFacade.addLogEntry(user, LogMessage.editUser,result);
       
    return Response
            .status(200)
            .entity(result)
            .build();
  }
  
  
  @Path("resetPasswordFor/{id}")
  @PUT
  @Produces("application/json")
  @Consumes("application/json")
  public Response changePasswordForUser(@PathParam("id") int id) throws PasswordStorage.CannotPerformOperationException{
    return Response
            .status(200)
            .entity(jsonAssembler.resetPassword(id))
            .build();
  }
  
  @RolesAllowed("Super") 
  @Path("deleteUser/{userId}")
  @DELETE
  @Produces("application/json")
  @Consumes("application/json")
  public Response deleteUser(@PathParam("userId") int id){
    String result = jsonAssembler.deleteUser(id);
    
    String user = securityContext.getUserPrincipal().getName();
    LogFacade.addLogEntry(user, LogMessage.deleteUser,result);
    
    return Response
            .status(200)
            .entity(result)
            .build();
  }
  
  @RolesAllowed("Super") 
  @Path("removeFromClass")
  @PUT
  @Produces("application/json")
  @Consumes("application/json")
  public Response removeStudentsFromClass(String data){
    jsonAssembler.removeStudentsFromClass(data);
    return Response
            .status(200)
            .build();
  }
  
  @Path("attendancecode/{taskid}")
  @GET
  @Produces("application/json")
  public Response getAttendanceCode(@PathParam("taskid") int taskid) throws NonexistentEntityException, StudyPointException{
     CacheControl cc = new CacheControl();
     cc.setMaxAge(60); 
 
    return Response
            .status(200)
            .entity(jsonAssembler.getAutoLoginCode(taskid))
            .cacheControl(cc)
            .build();
  }
  
  
  @Path("scores")
  @PUT
 // @Produces("application/json")
  @Consumes("application/json")
  public Response editPeriodJson(String scoresAsJson){
    jsonAssembler.editStudyPoints(scoresAsJson);
    String user = securityContext.getUserPrincipal().getName();
    LogFacade.addLogEntry(user, LogMessage.editPeriodJson);
    
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Origin, Authorization, Accept, Client-Security-Token, Accept-Encoding")
            .header("Access-Control-Allow-Credentials", "true")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            .header("Access-Control-Max-Age", "1209600")
            .build();
  }
  
  
  
  
//  @Path("scores")
//  @OPTIONS
// // @Produces("application/json")
//  @Consumes("application/json")
//  public Response xxx(String scoresAsJson){
//     return Response
//            .status(200)
//            .header("Access-Control-Allow-Origin", "*")
//            .header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Origin, Authorization, Accept, Client-Security-Token, Accept-Encoding")
//            .header("Access-Control-Allow-Credentials", "true")
//            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
//            .header("Access-Control-Max-Age", "1209600")
//            .build();
//  }

 
}
