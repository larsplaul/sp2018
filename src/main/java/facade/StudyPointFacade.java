package facade;

import deploy.DeploymentConfiguration;
import entity.*;
import entity.exceptions.NonexistentEntityException;
import entity.exceptions.StudyPointException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author plaul1
 */
public class StudyPointFacade implements Serializable {

  static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);

  public StudyPointFacade(EntityManagerFactory emfac) {
    emf = emfac;
  }

  //Uses the static EntityManagerFactory
  public StudyPointFacade() {}
  
  /**
   * Use this to autoregister points for a studypoint, when the StudyPoint code
   * is known
   *
   * @param studyPointId
   * @param code
   * @param user
   * @throws StudyPointException
   */
  public String registerViaCode(int studyPointId, String code, String user, boolean ignoreCode) throws StudyPointException {

    EntityManager em = null;
    try {
      em = getEntityManager();
      StudyPoint studyPoint = em.find(StudyPoint.class, studyPointId);
      if (studyPoint==null) {
        throw new StudyPointException("No studypoint found with the provided ID");
      }
      if (!studyPoint.getStudyPointUser().getUserName().equals(user)) {
        throw new StudyPointException("You are not authorized to register for this study point");
      }
      String codeToMatch = studyPoint.getTask().getCode();
      if (codeToMatch == null) {
        throw new StudyPointException("Points could not be assigned, code was probably timed out");
      }
      if (!ignoreCode && !codeToMatch.equals(code)) {
        throw new StudyPointException("Wrong code");
      }
      studyPoint.setScore(studyPoint.getTask().getMaxScore());
      em.getTransaction().begin();
      em.merge(studyPoint);
      em.getTransaction().commit();
      return studyPoint.getTask().getName();
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }
   

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  public void create(StudyPoint studyPoint) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      StudyPointUser studyPointUser = studyPoint.getStudyPointUser();
      if (studyPointUser != null) {
        studyPointUser = em.getReference(studyPointUser.getClass(), studyPointUser.getId());
        studyPoint.setStudyPointUser(studyPointUser);
      }
      em.persist(studyPoint);
      if (studyPointUser != null) {
        studyPointUser.getStudyPoints().add(studyPoint);
        studyPointUser = em.merge(studyPointUser);
      }
      em.getTransaction().commit();
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public StudyPoint edit(int id, int score) throws NonexistentEntityException, Exception {
    EntityManager em = null;
    StudyPoint studyPoint;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      studyPoint = em.find(StudyPoint.class, id);
      if (studyPoint == null) {
        throw new NonexistentEntityException("The studyPoint with id " + id + " no longer exists.");
      }
      studyPoint.setScore(score);
      studyPoint = em.merge(studyPoint);
      em.getTransaction().commit();
    } catch (Exception ex) {
      throw ex;
    } finally {
      if (em != null) {
        em.close();
      }
    }
    return studyPoint;
  }

  //TODO --> I think this is a VERY expensive method, refactor to use JOINS
  /*
    Caller must provide the EntityManager, and OPEN and CLOSE (Commit) the transaction
   */
  public static void setStudyPoint(EntityManager em, int newScore, String userName, String taskName, String periodName, String classId) throws StudyPointException {

    String queryString = "Select s from StudyPoint s "
            + "where s.studyPointUser.userName = :username "
            + "and (s.task.name = :taskname) "
            + "and (s.task.semesterPeriod.periodName = :periodname) "
            + "and (s.task.semesterPeriod.inClass.id = :classid)";

    Query query = em.createQuery(queryString);
    query.setParameter("username", userName);
    query.setParameter("taskname", taskName);
    query.setParameter("periodname", periodName);
    query.setParameter("classid", classId);

    StudyPoint sp = (StudyPoint) query.getSingleResult();
    sp.setScore(newScore);
    em.merge(sp);
  }

  public void destroy(Integer id) throws NonexistentEntityException {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      StudyPoint studyPoint;
      try {
        studyPoint = em.getReference(StudyPoint.class, id);
        studyPoint.getId();
      } catch (EntityNotFoundException enfe) {
        throw new NonexistentEntityException("The studyPoint with id " + id + " no longer exists.", enfe);
      }
      StudyPointUser studyPointUser = studyPoint.getStudyPointUser();
      if (studyPointUser != null) {
        studyPointUser.getStudyPoints().remove(studyPoint);
        studyPointUser = em.merge(studyPointUser);
      }
      em.remove(studyPoint);
      em.getTransaction().commit();
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public List<StudyPoint> findStudyPointEntities() {
    return findStudyPointEntities(true, -1, -1);
  }

  public List<StudyPoint> findStudyPointEntities(int maxResults, int firstResult) {
    return findStudyPointEntities(false, maxResults, firstResult);
  }

  private List<StudyPoint> findStudyPointEntities(boolean all, int maxResults, int firstResult) {
    EntityManager em = getEntityManager();
    try {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      cq.select(cq.from(StudyPoint.class));
      Query q = em.createQuery(cq);
      if (!all) {
        q.setMaxResults(maxResults);
        q.setFirstResult(firstResult);
      }
      return q.getResultList();
    } finally {
      em.close();
    }
  }

  public StudyPoint findStudyPoint(Integer id) {
    EntityManager em = getEntityManager();
    try {
      return em.find(StudyPoint.class, id);
    } finally {
      em.close();
    }
  }

  public int getStudyPointCount() {
    EntityManager em = getEntityManager();
    try {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      Root<StudyPoint> rt = cq.from(StudyPoint.class);
      cq.select(em.getCriteriaBuilder().count(rt));
      Query q = em.createQuery(cq);
      return ((Long) q.getSingleResult()).intValue();
    } finally {
      em.close();
    }
  }

}
