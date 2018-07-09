/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import entity.exceptions.NonexistentEntityException;
import entity.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author plaul1
 */
public class SP_ClassJpaController implements Serializable {

  public SP_ClassJpaController(EntityManagerFactory emf) {
    this.emf = emf;
  }
  private EntityManagerFactory emf = null;

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  public void create(SP_Class SP_Class) throws PreexistingEntityException, Exception {
    if (SP_Class.getPeriods() == null) {
      SP_Class.setPeriods(new ArrayList<SemesterPeriod>());
    }
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      List<SemesterPeriod> attachedPeriods = new ArrayList<SemesterPeriod>();
      for (SemesterPeriod periodsSemesterPeriodToAttach : SP_Class.getPeriods()) {
        periodsSemesterPeriodToAttach = em.getReference(periodsSemesterPeriodToAttach.getClass(), periodsSemesterPeriodToAttach.getId());
        attachedPeriods.add(periodsSemesterPeriodToAttach);
      }
      SP_Class.setPeriods(attachedPeriods);
      em.persist(SP_Class);
      for (SemesterPeriod periodsSemesterPeriod : SP_Class.getPeriods()) {
        SP_Class oldInClassOfPeriodsSemesterPeriod = periodsSemesterPeriod.getInClass();
        periodsSemesterPeriod.setInClass(SP_Class);
        periodsSemesterPeriod = em.merge(periodsSemesterPeriod);
        if (oldInClassOfPeriodsSemesterPeriod != null) {
          oldInClassOfPeriodsSemesterPeriod.getPeriods().remove(periodsSemesterPeriod);
          oldInClassOfPeriodsSemesterPeriod = em.merge(oldInClassOfPeriodsSemesterPeriod);
        }
      }
      em.getTransaction().commit();
    } catch (Exception ex) {
      if (findSP_Class(SP_Class.getId()) != null) {
        throw new PreexistingEntityException("SP_Class " + SP_Class + " already exists.", ex);
      }
      throw ex;
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
      SP_Class persistentSP_Class = em.find(SP_Class.class, SP_Class.getId());
      List<SemesterPeriod> periodsOld = persistentSP_Class.getPeriods();
      List<SemesterPeriod> periodsNew = SP_Class.getPeriods();
      List<SemesterPeriod> attachedPeriodsNew = new ArrayList<SemesterPeriod>();
      for (SemesterPeriod periodsNewSemesterPeriodToAttach : periodsNew) {
        periodsNewSemesterPeriodToAttach = em.getReference(periodsNewSemesterPeriodToAttach.getClass(), periodsNewSemesterPeriodToAttach.getId());
        attachedPeriodsNew.add(periodsNewSemesterPeriodToAttach);
      }
      periodsNew = attachedPeriodsNew;
      SP_Class.setPeriods(periodsNew);
      SP_Class = em.merge(SP_Class);
      for (SemesterPeriod periodsOldSemesterPeriod : periodsOld) {
        if (!periodsNew.contains(periodsOldSemesterPeriod)) {
          periodsOldSemesterPeriod.setInClass(null);
          periodsOldSemesterPeriod = em.merge(periodsOldSemesterPeriod);
        }
      }
      for (SemesterPeriod periodsNewSemesterPeriod : periodsNew) {
        if (!periodsOld.contains(periodsNewSemesterPeriod)) {
          SP_Class oldInClassOfPeriodsNewSemesterPeriod = periodsNewSemesterPeriod.getInClass();
          periodsNewSemesterPeriod.setInClass(SP_Class);
          periodsNewSemesterPeriod = em.merge(periodsNewSemesterPeriod);
          if (oldInClassOfPeriodsNewSemesterPeriod != null && !oldInClassOfPeriodsNewSemesterPeriod.equals(SP_Class)) {
            oldInClassOfPeriodsNewSemesterPeriod.getPeriods().remove(periodsNewSemesterPeriod);
            oldInClassOfPeriodsNewSemesterPeriod = em.merge(oldInClassOfPeriodsNewSemesterPeriod);
          }
        }
      }
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

  public void destroy(String id) throws NonexistentEntityException {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      SP_Class SP_Class;
      try {
        SP_Class = em.getReference(SP_Class.class, id);
        SP_Class.getId();
      } catch (EntityNotFoundException enfe) {
        throw new NonexistentEntityException("The SP_Class with id " + id + " no longer exists.", enfe);
      }
      List<SemesterPeriod> periods = SP_Class.getPeriods();
      for (SemesterPeriod periodsSemesterPeriod : periods) {
        periodsSemesterPeriod.setInClass(null);
        periodsSemesterPeriod = em.merge(periodsSemesterPeriod);
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
      return em.find(SP_Class.class, id);
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
