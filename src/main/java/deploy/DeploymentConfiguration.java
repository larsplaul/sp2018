package deploy;

import entity.StudyPointUser;
import entity.Task;
import entity.UserRole;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import rest.TestResource;
import security.MailSender;
import security.Secrets;

@WebListener
public class DeploymentConfiguration implements ServletContextListener {

  public static String PU_NAME = "PU-Local";

  @Override
  @SuppressWarnings("empty-statement")
  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("######################################################################################");
    System.out.println("############################ In ContextIntialized ####################################");
    System.out.println("######################################################################################");

    //Handling init-params from the properties file (secrets that should not be pushed to GIT)
    InputStream input = null;
    Properties prop = new Properties();
    try {
      input = getClass().getClassLoader().getResourceAsStream("/config.properties");;
      if (input == null) {
        System.out.println("Could not load init-properties");
        return;
      }
      prop.load(input);

      //TODO Figure out whether this should be given via props, or via a DB-table
//      String ips = prop.getProperty("ips");
//      if (ips != null) {
//        utils.ValidIps.setValidIps(ips);
//      }
      
      
  
      System.out.println(String.format("Mail: %1$s, %2$s, %3$s",prop.getProperty("mailServer"), prop.getProperty("mailUser"), prop.getProperty("mailPassword")));
      MailSender.initConstants(prop.getProperty("mailServer"), prop.getProperty("mailUser"), prop.getProperty("mailPassword"));
      Secrets.SHARED_SECRET = prop.getProperty("tokenSecret").getBytes();
      input.close();

    } catch (IOException ex) {
      Logger.getLogger(DeploymentConfiguration.class.getName()).log(Level.SEVERE, null, ex);
    }
    ServletContext context = sce.getServletContext();
    Map<String, String> env = System.getenv();
    System.out.println("LISTING ALL KEYS");
    for (String k : env.keySet()) {
      System.out.println(k);
    }

    //If we are running in the OPENSHIFT environment change the pu-name 
    if (env.keySet().contains("OPENSHIFT_MYSQL_DB_HOST")) {
      PU_NAME = "PU_OPENSHIFT";
    }


    /*
  If we are running on a Digital Ocean Droplet change the pu-name 
  Important: Remember when installing Tomcat on DO to set this Environment Variable.

  For Tomcat 8 it must be set in the the file: /etc/default/tomcat8 as:
  IS_ON_DIGITAL_OCEAN=true

  Tomcat DOES NOT READ general Environment Variables, so it must be set in this file 
     */
    if (env.keySet().contains("IS_ON_DIGITAL_OCEAN")) {
      PU_NAME = "PU_DIGITAL_OCEAN";
    }
    if (env.keySet().contains("IS_ON_DIGITAL_OCEAN_SP")) {
      PU_NAME = "PU_DIGITAL_OCEAN_STUDYPOINTS";
    }
    System.out.println("PU_NAME: " + PU_NAME);

    
    utils.ValidIps.setValidIpsFromDB();
    //Handling init-params from web.xml
    boolean isDebug = context.getInitParameter("debug").toLowerCase().equals("true");

//    MailSender.initConstants(context);
    isDebug = isDebug || context.getInitParameter("makeTestUser").toLowerCase().equals("true");
    TestResource.STATUS = isDebug ? "DEBUG" : "PRODUCTION";

    StudyPointUser.tempPasswordTimeoutMinutes = Integer.parseInt(context.getInitParameter("tempPasswordTimeoutMinutes"));
    Task.CODE_TIMEOUT_MINUTES = Integer.parseInt(context.getInitParameter("autoAttendaceCodeTimeOutMinutes"));

    boolean makeTestUser = context.getInitParameter("makeTestUser").toLowerCase().equals("true");
    if (makeTestUser) {
      System.out.println("Making Test Usr: lam");
      EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
      EntityManager em = emf.createEntityManager();
      try {
        UserRole student = new UserRole("User");
        UserRole admin = new UserRole("Admin");
        UserRole superRole = new UserRole("Super");
        StudyPointUser user = new StudyPointUser("lam", "lars", "mortensen", "lam@cphbusiness.dk", "");
        user.setPasswordInitial("");
        try {
          user.setPassword("test");
          em.persist(admin);
          em.persist(user);
          em.persist(superRole);
          //UserRole role = em.find(UserRole.class, "Admin");
          admin.addStudyPointUser(user);
          user.addRole(superRole);
          user.addRole(admin);
          em.getTransaction().begin();
          em.persist(student);
          em.getTransaction().commit();
        } catch (Exception ex) {
          //Logger.getLogger(DeploymentConfiguration.class.getName()).log(Level.SEVERE, null, ex);
          Logger.getLogger(DeploymentConfiguration.class.getName()).log(Level.SEVERE, "User already exist");
        }
      } finally {
        em.close();
        emf.close();
      }

    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

    //https://stackoverflow.com/questions/11872316/tomcat-guice-jdbc-memory-leak
//    Enumeration<Driver> drivers = DriverManager.getDrivers();
//    Driver d = null;
//    while (drivers.hasMoreElements()) {
//      try {
//        d = drivers.nextElement();
//        DriverManager.deregisterDriver(d);
//        Logger.getLogger(DeploymentConfiguration.class.getName()).log(Level.WARNING, String.format("Driver %s deregistered", d));
//
//      } catch (SQLException ex) {
//        Logger.getLogger(DeploymentConfiguration.class.getName()).log(Level.SEVERE, String.format("Error deregistering driver %s", d));
//
//      }
//    }
//    try {
//      AbandonedConnectionCleanupThread.shutdown();
//    } catch (InterruptedException e) {
//       Logger.getLogger(DeploymentConfiguration.class.getName()).log(Level.SEVERE, "Severe problem while cleaning up");
//    }
  }
}
