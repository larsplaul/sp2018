package rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import deploy.DeploymentConfiguration;
import facade.LogFacade;
import facade.StudyPointUserFacade;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import security.PasswordStorage;
import security.Secrets;
import facade.LogMessage;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import security.AuthenticatedUser;

@Path("login")
public class Login {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response login(@Context HttpServletRequest requestContext, String jsonString) throws JOSEException, AuthenticationException {
    String device = requestContext.getHeader("ClientDevice");  //Find a better way to do this
    String clientType = device!= null ? device: "browser";
    //System.out.println("DEVICE TYPE ----> "+clientType);
    JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
    String username = json.get("username").getAsString();
    String password = json.get("password").getAsString();
   // boolean useFronter = json.get("useFronter").getAsBoolean();
    JsonObject responseJson = new JsonObject();
    //String role;  
    //List<String> roles;
    AuthenticatedUser userDetails = null;
    try {
      if ((userDetails = authenticate(username, password)) != null) {
        String token = createToken(username, "lam@cphbusiness.dk", userDetails,clientType);
        responseJson.addProperty("username", username);
        responseJson.addProperty("token", token);
        LogFacade.addLogEntry(username, LogMessage.okLogin, clientType);
        return Response.ok(new Gson().toJson(responseJson)).header("Access-Control-Allow-Origin", "*").build();
      }
    } catch (PasswordStorage.CannotPerformOperationException | PasswordStorage.InvalidHashException ex) {
      AuthenticationException ae = new AuthenticationException("The system could not Authenticate you, with the provided credentials");
      ae.setRootCause(ex);
      throw ae;
    }
    throw new NotAuthorizedException("Ilegal username or password"); 
  }

  //Todo Deep deeper into this
  @OPTIONS
  @Produces("application/json")
  @Consumes("application/json")
  public Response loginOpt(String scoresAsJson) {
    return Response
            .status(200)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Origin, Authorization, Accept, Client-Security-Token, Accept-Encoding")
            .header("Access-Control-Allow-Credentials", "true")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            .header("Access-Control-Max-Age", "1209600")
            .build();
  }

  static AuthenticatedUser authenticate(String userName, String password) throws PasswordStorage.CannotPerformOperationException, PasswordStorage.InvalidHashException, AuthenticationException {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    StudyPointUserFacade facade = new StudyPointUserFacade(emf);
    return facade.authenticateUser(userName, password);
  }

  static String createToken(String subject, String issuer, AuthenticatedUser userDetails, String clientType) throws JOSEException {
    Date date = new Date();
    //Provide a long-lived token (four months) for mobile devices
    Date exportationTime = clientType.equals("browser") ?  new Date(date.getTime() + 1000 * 60 * 60): 
                                                           new Date(date.getTime() + 1000 * 60 * 60 * 24 * 120);
    StringBuilder res = new StringBuilder();
    JWSSigner signer = new MACSigner(Secrets.SHARED_SECRET);
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(subject)
            .claim("username", subject)
            .claim("roles", userDetails.getRoles())
            .claim("fn", userDetails.getFirstName())
            .claim("ln", userDetails.getLastName())
            .claim("issuer", issuer)
            .issueTime(date)
            //.expirationTime(new Date(date.getTime() + 1000 * 60 * 60))
            .expirationTime(exportationTime)
            .build();
    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
    signedJWT.sign(signer);
    return signedJWT.serialize();

  }
}
