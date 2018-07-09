/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deploy.DeploymentConfiguration;
import entity.StudyPointUser;
import entity.exceptions.NonexistentEntityException;
import facade.LogFacade;
import facade.LogMessage;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author plaul1
 */
@Path("resetpassword")
public class ResetPasswordService {

  @Context
  private UriInfo context;

  /**
   * Creates a new instance of ResetPasswordService
   */
  public ResetPasswordService() {
  }
 
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response resetPassword(String data) throws NonexistentEntityException, PasswordStorage.CannotPerformOperationException {
    JsonObject emailJson = new JsonParser().parse(data).getAsJsonObject();
    String email = emailJson.get("email").getAsString();
    EntityManager em = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME).createEntityManager();
    try{
      StudyPointUser user = (StudyPointUser)em.createNamedQuery("StudyPointUser.findByEmail").setParameter("email", email).getSingleResult();
      String tempPassword = new TempPasswordGenerator().nextPassword();
      System.out.println("PW --------------------------> "+tempPassword);
      em.getTransaction().begin();
      user.setTempPassword(tempPassword);
      em.getTransaction().commit();
      System.out.println("Sending mail");
      MailSender.sendMail(user.getEmail(), user.getUserName(), tempPassword);
      LogFacade.addLogEntry(user.getUserName(), LogMessage.newPWViaMail);
      System.out.println("Sent mail");
      return Response.status(200)
            .entity("{\"status\":"+"\"OK\""+"}")
            .type(MediaType.APPLICATION_JSON).
            build();
      
      
    }
    catch(javax.persistence.NoResultException ex){
      throw new NonexistentEntityException("Email does not belong to any registered user",ex);
    }
    finally{
      em.getEntityManagerFactory().close();
      //em.close();
    }
    
  }
}
