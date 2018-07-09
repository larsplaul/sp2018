package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import deploy.DeploymentConfiguration;
import facade.StudyPointFacade;
import java.text.ParseException;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import jsonmappers.RemoteStudyPointAssign;
import security.Secrets;

//TODO - Add a NEW security role for remote access
@Path("remoteuser")
@RolesAllowed("Admin") 
public class ThirdPartAccess {

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
  static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response remoteStudyPointAssign(String jsonString) throws ParseException, JOSEException, Exception {
    RemoteStudyPointAssign data = gson.fromJson(jsonString, RemoteStudyPointAssign.class);

    SignedJWT signedJWT = SignedJWT.parse(data.userToken);
    JWSVerifier verifier = new MACVerifier(Secrets.SHARED_SECRET);
    EntityManager em = emf.createEntityManager();
    if (signedJWT.verify(verifier)) {
      data.userToken = "Got this user name from the Token: " + signedJWT.getJWTClaimsSet().getClaim("username");
    }
    try {
      em.getTransaction().begin();
      String user = signedJWT.getJWTClaimsSet().getClaim("username").toString();
      StudyPointFacade.setStudyPoint(em, data.score, user, data.taskName, data.periodName, data.classId);
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
      throw e;
    } finally {
      em.close();
    }

    return Response.
            ok(new Gson().toJson(data)).build();
  }
}
