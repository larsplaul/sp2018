package rest;

import facade.JsonAssembler;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("studyAdministration")
@RolesAllowed("Admin") 
public class StudyAdministration {

  @Context
  SecurityContext securityContext;

  private final JsonAssembler jsonAssembler;

  public StudyAdministration() {
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

  @Path("pointsForAllStudents/{classId}")
  @GET
  @Produces("application/json")
  public Response totalStudypointsForStudentInClass(@PathParam("classId") String classId) {
    
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getTotalStudyPointsForSemester(classId))
            .build();
  }

  @Path("studentStudypoint/{classId}/{studentId}")
  @GET
  @Produces("application/json")
  public Response studypointsForStudentInClass(@PathParam("classId") String classId, @PathParam("studentId") int studentId) {
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .entity(jsonAssembler.getStudyPointsForStudent(classId, studentId,false))
            .build();
  }

}
