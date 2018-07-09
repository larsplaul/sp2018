
package facade;

import entity.SP_Class;
import entity.exceptions.NonexistentEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


public class ClassFacade implements Serializable {
  

  public ClassFacade(EntityManagerFactory emf) {
    this.emf = emf;
  }
  private EntityManagerFactory emf = null;

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  public SP_Class create(SP_Class newClass) {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      em.persist(newClass);
      em.getTransaction().commit();
      return newClass;
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public void edit(SP_Class SP_Class) throws NonexistentEntityException, Exception {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      SP_Class = em.merge(SP_Class);
      em.getTransaction().commit();
    } catch (Exception ex) {
      String msg = ex.getLocalizedMessage();
      if (msg == null || msg.length() == 0) {
        String id = SP_Class.getId();
        if (findSP_Class(id) == null) {
          throw new NonexistentEntityException("The sP_Class with id " + id + " no longer exists.");
        }
      }
      throw ex;
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public void destroy(Integer id) throws NonexistentEntityException {
    EntityManager em = getEntityManager();
    try {
      
      em.getTransaction().begin();
      SP_Class SP_Class;
      try {
        SP_Class = em.getReference(SP_Class.class, id);
        SP_Class.getId();
      } catch (EntityNotFoundException enfe) {
        throw new NonexistentEntityException("The SP_Class with id " + id + " no longer exists.", enfe);
      }
      em.remove(SP_Class);
      em.getTransaction().commit();
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public List<SP_Class> findSP_ClassEntities() {
    return findSP_ClassEntities(true, -1, -1);
  }

  public List<SP_Class> findSP_ClassEntities(int maxResults, int firstResult) {
    return findSP_ClassEntities(false, maxResults, firstResult);
  }

  private List<SP_Class> findSP_ClassEntities(boolean all, int maxResults, int firstResult) {
    EntityManager em = getEntityManager();
    try {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      cq.select(cq.from(SP_Class.class));
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

  public SP_Class findSP_Class(String id) {
    EntityManager em = getEntityManager();
    try {
      SP_Class c = em.find(SP_Class.class, id);
      //c.getPeriods();
      return c;
    } finally {
      em.close();
    }
  }

  public int getSP_ClassCount() {
    EntityManager em = getEntityManager();
    try {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      Root<SP_Class> rt = cq.from(SP_Class.class);
      cq.select(em.getCriteriaBuilder().count(rt));
      Query q = em.createQuery(cq);
      return ((Long) q.getSingleResult()).intValue();
    } finally {
      em.close();
    }
  }
  
}
