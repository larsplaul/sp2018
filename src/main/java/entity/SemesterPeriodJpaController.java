/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import entity.exceptions.NonexistentEntityException;
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
public class SemesterPeriodJpaController implements Serializable {

  public SemesterPeriodJpaController(EntityManagerFactory emf) {
    this.emf = emf;
  }
  private EntityManagerFactory emf = null;

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  public void create(SemesterPeriod semesterPeriod) {
    if (semesterPeriod.getTasks() == null) {
      semesterPeriod.setTasks(new ArrayList<Task>());
    }
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      SP_Class inClass = semesterPeriod.getInClass();
      if (inClass != null) {
        inClass = em.getReference(inClass.getClass(), inClass.getId());
        semesterPeriod.setInClass(inClass);
      }
      List<Task> attachedTasks = new ArrayList<Task>();
      for (Task tasksTaskToAttach : semesterPeriod.getTasks()) {
        tasksTaskToAttach = em.getReference(tasksTaskToAttach.getClass(), tasksTaskToAttach.getId());
        attachedTasks.add(tasksTaskToAttach);
      }
      semesterPeriod.setTasks(attachedTasks);
      em.persist(semesterPeriod);
      if (inClass != null) {
        inClass.getPeriods().add(semesterPeriod);
        inClass = em.merge(inClass);
      }
      for (Task tasksTask : semesterPeriod.getTasks()) {
        SemesterPeriod oldSemesterPeriodOfTasksTask = tasksTask.getSemesterPeriod();
        tasksTask.setSemesterPeriod(semesterPeriod);
        tasksTask = em.merge(tasksTask);
        if (oldSemesterPeriodOfTasksTask != null) {
          oldSemesterPeriodOfTasksTask.getTasks().remove(tasksTask);
          oldSemesterPeriodOfTasksTask = em.merge(oldSemesterPeriodOfTasksTask);
        }
      }
      em.getTransaction().commit();
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public void edit(SemesterPeriod semesterPeriod) throws NonexistentEntityException, Exception {
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      SemesterPeriod persistentSemesterPeriod = em.find(SemesterPeriod.class, semesterPeriod.getId());
      SP_Class inClassOld = persistentSemesterPeriod.getInClass();
      SP_Class inClassNew = semesterPeriod.getInClass();
      List<Task> tasksOld = persistentSemesterPeriod.getTasks();
      List<Task> tasksNew = semesterPeriod.getTasks();
      if (inClassNew != null) {
        inClassNew = em.getReference(inClassNew.getClass(), inClassNew.getId());
        semesterPeriod.setInClass(inClassNew);
      }
      List<Task> attachedTasksNew = new ArrayList<Task>();
      for (Task tasksNewTaskToAttach : tasksNew) {
        tasksNewTaskToAttach = em.getReference(tasksNewTaskToAttach.getClass(), tasksNewTaskToAttach.getId());
        attachedTasksNew.add(tasksNewTaskToAttach);
      }
      tasksNew = attachedTasksNew;
      semesterPeriod.setTasks(tasksNew);
      semesterPeriod = em.merge(semesterPeriod);
      if (inClassOld != null && !inClassOld.equals(inClassNew)) {
        inClassOld.getPeriods().remove(semesterPeriod);
        inClassOld = em.merge(inClassOld);
      }
      if (inClassNew != null && !inClassNew.equals(inClassOld)) {
        inClassNew.getPeriods().add(semesterPeriod);
        inClassNew = em.merge(inClassNew);
      }
      for (Task tasksOldTask : tasksOld) {
        if (!tasksNew.contains(tasksOldTask)) {
          tasksOldTask.setSemesterPeriod(null);
          tasksOldTask = em.merge(tasksOldTask);
        }
      }
      for (Task tasksNewTask : tasksNew) {
        if (!tasksOld.contains(tasksNewTask)) {
          SemesterPeriod oldSemesterPeriodOfTasksNewTask = tasksNewTask.getSemesterPeriod();
          tasksNewTask.setSemesterPeriod(semesterPeriod);
          tasksNewTask = em.merge(tasksNewTask);
          if (oldSemesterPeriodOfTasksNewTask != null && !oldSemesterPeriodOfTasksNewTask.equals(semesterPeriod)) {
            oldSemesterPeriodOfTasksNewTask.getTasks().remove(tasksNewTask);
            oldSemesterPeriodOfTasksNewTask = em.merge(oldSemesterPeriodOfTasksNewTask);
          }
        }
      }
      em.getTransaction().commit();
    } catch (Exception ex) {
      String msg = ex.getLocalizedMessage();
      if (msg == null || msg.length() == 0) {
        Integer id = semesterPeriod.getId();
        if (findSemesterPeriod(id) == null) {
          throw new NonexistentEntityException("The semesterPeriod with id " + id + " no longer exists.");
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
    EntityManager em = null;
    try {
      em = getEntityManager();
      em.getTransaction().begin();
      SemesterPeriod semesterPeriod;
      try {
        semesterPeriod = em.getReference(SemesterPeriod.class, id);
        semesterPeriod.getId();
      } catch (EntityNotFoundException enfe) {
        throw new NonexistentEntityException("The semesterPeriod with id " + id + " no longer exists.", enfe);
      }
      SP_Class inClass = semesterPeriod.getInClass();
      if (inClass != null) {
        inClass.getPeriods().remove(semesterPeriod);
        inClass = em.merge(inClass);
      }
      List<Task> tasks = semesterPeriod.getTasks();
      for (Task tasksTask : tasks) {
        tasksTask.setSemesterPeriod(null);
        tasksTask = em.merge(tasksTask);
      }
      em.remove(semesterPeriod);
      em.getTransaction().commit();
    } finally {
      if (em != null) {
        em.close();
      }
    }
  }

  public List<SemesterPeriod> findSemesterPeriodEntities() {
    return findSemesterPeriodEntities(true, -1, -1);
  }

  public List<SemesterPeriod> findSemesterPeriodEntities(int maxResults, int firstResult) {
    return findSemesterPeriodEntities(false, maxResults, firstResult);
  }

  private List<SemesterPeriod> findSemesterPeriodEntities(boolean all, int maxResults, int firstResult) {
    EntityManager em = getEntityManager();
    try {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      cq.select(cq.from(SemesterPeriod.class));
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

  public SemesterPeriod findSemesterPeriod(Integer id) {
    EntityManager em = getEntityManager();
    try {
      return em.find(SemesterPeriod.class, id);
    } finally {
      em.close();
    }
  }

  public int getSemesterPeriodCount() {
    EntityManager em = getEntityManager();
    try {
      CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
      Root<SemesterPeriod> rt = cq.from(SemesterPeriod.class);
      cq.select(em.getCriteriaBuilder().count(rt));
      Query q = em.createQuery(cq);
      return ((Long) q.getSingleResult()).intValue();
    } finally {
      em.close();
    }
  }
  
}
