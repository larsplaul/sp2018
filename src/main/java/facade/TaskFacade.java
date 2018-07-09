package facade;

import deploy.DeploymentConfiguration;
import entity.Log_Info;
import entity.StudyPoint;
import entity.Task;
import entity.exceptions.NonexistentEntityException;
import entity.exceptions.StudyPointException;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import security.RandomStringGenerator;

/**
 *
 * @author plaul1
 */
public class TaskFacade {
static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    
    static private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    public static String createTaskCode(int taskId) throws NonexistentEntityException, StudyPointException{
      EntityManager em = getEntityManager();
      String code = null;
      try{
        Task task = em.find(Task.class, taskId);
        if(task == null){
          throw new NonexistentEntityException("Task not found");
        }
        if(!task.isAllowAutoAttendance()){
          throw new StudyPointException("Auto Attendance not allowed for this task");
        }
        code = RandomStringGenerator.getCode();
        task.setCode(code);
        em.getTransaction().begin();
        em.merge(task);
        em.getTransaction().commit();
      }
      finally{
        em.close();
      }
      return code;
    }
    
    public static void registerAttendance(int userID, int taskId, int studyPointId, String code) throws StudyPointException{
      EntityManager em = getEntityManager();
      try{
        Task task = em.find(Task.class, taskId);
        if(task == null){
          throw new StudyPointException("Task not found");
        }
        if(!task.hasValidCode(new Date())){
          throw new StudyPointException("No code provided for this task, or code has timed out");
        }
        if(!task.getCode().equals(code.toUpperCase())){
          throw new StudyPointException("Wrong Code");
        }
        StudyPoint sp = task.getStudyPointForStudent(userID);
        if(sp.getId() == studyPointId){
          sp.setScore(task.getMaxScore());
          em.getTransaction().begin();
          em.merge(sp);
          em.getTransaction().commit();          
        }
      }
      catch (Exception e){
        if(e instanceof StudyPointException){
          throw e;
        }
        throw new StudyPointException("Attendance could not be registered");
      }
      finally{
        em.close();
      }
      
    }
   
}
