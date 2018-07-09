package utils;

import deploy.DeploymentConfiguration;
import entity.ValidIp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Contains a list of public IP's used by the school
 * The assumption will be, if a request commes from one of these ip's, it commes from the school
 * This will let at student register without a code
 */
public class ValidIps {
  private static Map<String,Boolean> validIps = new HashMap(); 
  
  //Should only be called from DeploymentConfiguration
  // This version assumes IP's are give via config.properties
  public static void setValidIps(String ipsAsString){
    String[] ips = ipsAsString.split(",");
    for(String ip : ips){
      validIps.put(ip, Boolean.TRUE);
    }
  }
  
  
  
  //Should be called from DeploymentConfiguration
  // This version reads from the DB
  public static void setValidIpsFromDB(){
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    try{
      setValidIpsFromDB(emf);
    }finally{
      emf.close();
    }
  }
  private static void setValidIpsFromDB(EntityManagerFactory emf){
    EntityManager em = emf.createEntityManager();
    validIps.clear();
    try{
      List<ValidIp> ips= em.createQuery("select ip from ValidIp ip").getResultList();
      for(ValidIp ip : ips){
        validIps.put(ip.getIp(), Boolean.TRUE);
      }  
    }finally{
      em.close();
    }
  }
  
  public static void addValidIpsToDB(List<String> ipsToAdd, boolean clearAllFirst){
    if(ipsToAdd == null){
      return;
    }
   
    EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    EntityManager em = emf.createEntityManager();
    try{
      List<String> newIPs = new ArrayList();
      for(String ip: ipsToAdd){
        if(em.find(ValidIp.class, ip)==null){
          newIPs.add(ip);
        }
      }  
      em.getTransaction().begin();
      
      if(clearAllFirst){
        em.createQuery("delete from ValidIp").executeUpdate();
      }
      
      for(String newIp : newIPs){
        em.persist(new ValidIp(newIp));
      }
      em.getTransaction().commit();
      setValidIpsFromDB(emf);
    }finally{
      em.close();
      emf.close();
    }
    
  }
  
  public static boolean isAValidIP(String ip){
    return validIps.get(ip) != null; 
  }
  
}
