package facade;

import deploy.DeploymentConfiguration;
import entity.SP_Class;
import entity.SemesterPeriod;
import entity.exceptions.NonexistentEntityException;

import static facade.TaskFacade.emf;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author plaul1
 */
public class PeriodFacade {

  static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
  
  private EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  public  SemesterPeriod getPeriodFromClassAndDescription(String classId, String periodDescription) {
    EntityManager em = getEntityManager();
    try {
      Query q = em.createQuery("SELECT p FROM SemesterPeriod p JOIN p.inClass t WHERE p.periodName = :periodName and p.inClass.id = :classId");
      q.setParameter("periodName", periodDescription);
      q.setParameter("classId", classId);
      SemesterPeriod p = (SemesterPeriod) q.getSingleResult();
      return p;
    } finally {
      em.close();
    }
  }
}
