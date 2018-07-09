package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import entity.StudyPoint;
import entity.exceptions.StudyPointException;
import facade.JsonAssembler;
import facade.LogFacade;
import facade.LogMessage;
import facade.StudyPointFacade;
import facade.StudyPointUserFacade;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import jsonmappers.StudyPointWithTaskInfo;

@Path("user")
@RolesAllowed("User")
public class Student {

  @Context
  SecurityContext securityContext;

  private final JsonAssembler jsonAssembler;
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public Student() {
    jsonAssembler = new JsonAssembler();
  }

  @Path("myClasses")
  @GET
  @Produces("application/json")
  public Response getClassesForCurrentUser() {
    String user = securityContext.getUserPrincipal().getName();
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getClassesForCurrentUser(user))
            .build();
  }

  @Path("myStudyPoints/{classId}")
  @GET
  @Produces("application/json")
  public Response studypointsForStudentClass(@HeaderParam("X-Forwarded-For") String clientIp,
                                             @Context HttpServletRequest requestContext,
                                             @PathParam("classId") String classId) {
    
     CacheControl cc = new CacheControl();
     cc.setNoCache(true);
     cc.setNoStore(true);
     cc.setMustRevalidate(true);
    
    String user = securityContext.getUserPrincipal().getName();
    boolean isValidIP = isValidIpForAutoRegister(clientIp, requestContext);
    
    LogFacade.addLogEntry(user, LogMessage.userGotPoints);
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .cacheControl(cc)
            .entity(jsonAssembler.getStudyPointsForCurrentUser(classId, user,isValidIP))
            .build();
  }


  
  @Path("registerAttendance")
  @PUT
  @Produces("application/json")
  public Response registerAttendence(@HeaderParam("X-Forwarded-For") String clientIp,
                                     @Context HttpServletRequest requestContext,
                                     String json) throws StudyPointException {
    
    jsonmappers.StudyPointMapper pointInfo = gson.fromJson(json, jsonmappers.StudyPointMapper.class);
    
    boolean ignoreCode = isValidIpForAutoRegister(clientIp, requestContext);
    System.out.println("Ip was OK: "+ignoreCode);
    String user = securityContext.getUserPrincipal().getName();
    try {
      new StudyPointFacade().registerViaCode(pointInfo.spId, pointInfo.code, user,ignoreCode);
      LogFacade.addLogEntry(user, LogMessage.registeredAttendance);
    } catch (Exception e) {
      LogFacade.addLogEntry(user, LogMessage.registerAttendanceFailded, e.getMessage());
      throw e;
    }
    String returnJson = "{\"status\":\"OK\"}";
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(returnJson)
            .build();
  }

  private boolean isValidIpForAutoRegister(String clientIp, HttpServletRequest requestContext) {
    //This is mainly to allow for local testing (remember to add 127.0.0.1 to the list of valid IP's)
    String clients_Ip = clientIp != null? clientIp :requestContext.getRemoteAddr();
    //System.out.println("ClientIP: "+ clients_Ip);
    //System.out.println("IP from Proxy: "+ clientIp);
    boolean validIP = utils.ValidIps.isAValidIP(clients_Ip);
    return validIP;
  }
/*
  Meant to be used from mobile client
  */
  @Path("registerViaCode")
  @PUT
  @Produces("application/json")
  public Response registerViaCode(@Context HttpServletRequest requestContext,String json) throws StudyPointException {
    jsonmappers.StudyPointMapper pointInfo = gson.fromJson(json, jsonmappers.StudyPointMapper.class);
    
    String locationVerified = requestContext.getHeader("LocationOK");  //Find a better way to do this
    boolean ignoreCode = locationVerified != null;
    String taskThatWasRegistered="";
    String user = securityContext.getUserPrincipal().getName();
    try {
       taskThatWasRegistered= new StudyPointFacade().registerViaCode(pointInfo.spId, pointInfo.code, user,ignoreCode);
      LogFacade.addLogEntry(user, LogMessage.registeredAttendance);
    } catch (Exception e) {
      LogFacade.addLogEntry(user, LogMessage.registerAttendanceFailded, e.getMessage());
      throw e;
    }
    String returnJson = String.format("{\"pointsRegisteredFor\":\"%s\"}",taskThatWasRegistered);
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(returnJson)
            .build();
  }

  @Path("getPointsWithValidCode")
  @GET
  @Produces("application/json")
  public Response getPointsWithValidCode() {
    String user = securityContext.getUserPrincipal().getName();
    List<StudyPointWithTaskInfo> mappedPoints = new ArrayList();
    for (StudyPoint p : new StudyPointUserFacade().getPointsToAutoRegister(user)) {
      mappedPoints.add(new StudyPointWithTaskInfo(p));
    }
    return Response
            .status(200)
            .entity(gson.toJson(mappedPoints))
            .build();
  }

}
