/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facade;

import deploy.DeploymentConfiguration;
import entity.Log_Info;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author plaul1
 */
public class LogFacade {
   static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    
    static private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public static void addLogEntry(String userName,String event){
      EntityManager em = getEntityManager();
      try{
        em.getTransaction().begin();
        Log_Info li = new Log_Info(userName, event);
        em.persist(li);
        em.getTransaction().commit();
      }finally{
        em.close();
      }
    }
    public static void addLogEntry(String userName,String event,String details){
      EntityManager em = getEntityManager();
      try{
        em.getTransaction().begin();
        //Details not used, until column is changed to handle more than 255 chars
        // 150 to save space
        if(details.length() >=150){
          details = details.substring(0,150);
        } 
        Log_Info li = new Log_Info(userName, event,details);
        em.persist(li);
        em.getTransaction().commit();
      }finally{
        em.close();
      }
    }
}
