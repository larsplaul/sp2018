
package scripthandling;

import entity.SP_Class;
import entity.SemesterPeriod;
import entity.StudyPoint;
import entity.StudyPointUser;
import entity.Task;
import entity.exceptions.ScriptException;
import java.util.Scanner;
import javax.persistence.EntityManager;

/**
 *
 * @author plaul1
 */
public class PeriodInfoStrategy extends ScriptHandler {
  
  public static final String TASKS = "_Tasks_";
  public static final String AUTO_ASSIGN = "_A_";

  protected PeriodInfoStrategy(String script, String separator) {
    super(script,separator);
  }
  
  public SemesterPeriod makePeriod(SP_Class theClass, String name, String description, EntityManager em) throws ScriptException {

    for (SemesterPeriod period : theClass.getPeriods()) {
      if (period.getPeriodName().equals(name)) {
        throw new ScriptException(makeError(String.format("The class '%1s' already contains a period with the name %2s ", theClass.getId(), name)));
      }
    }

    SemesterPeriod period = new SemesterPeriod();
    period.setInClass(theClass);
    period.setPeriodDescription(description);
    period.setPeriodName(name);
    theClass.addPeriod(period);
    em.merge(theClass);
    em.persist(period);
    return period;
  }

  public Task makeTask(int maxScore, String _name, SP_Class theClass, SemesterPeriod period, EntityManager em) {
    boolean allowAutoAttendance = false;
    String name = _name;
    if (name.startsWith(AUTO_ASSIGN)) {
      name = name.replaceFirst(AUTO_ASSIGN, "");
      name = name.trim();
      allowAutoAttendance = true;
    }
    Task task = new Task(maxScore, name, period, allowAutoAttendance);

    period.addTask(task);
    for (StudyPointUser user : theClass.getUsers()) {
      StudyPoint sp = new StudyPoint(task, user);
      em.persist(sp);
      user.addStudyPoint(sp);
      em.merge(user);
      task.addStudyPoint(sp);
    }
    em.persist(task);
    return task;
  }

  @Override
  protected void handleScriptTemplate(Scanner scan) throws ScriptException {
    boolean taskTagFound = false;
    EntityManager em = emf.createEntityManager();
    try {
      SP_Class theClass = null;
      SemesterPeriod period = null;
      em.getTransaction().begin();
      while (scan.hasNext()) {
        String line = getNextLine(scan);
        if(isLineToSkip(line)){
          continue;
        }
        if (period == null) {
          String[] periodInfo = line.split(SEPARATOR);
          String classID = periodInfo[0];
          theClass = em.find(SP_Class.class, classID);
          if (periodInfo.length != 3) {
            throw new ScriptException(makeError("Line following a '#PeriodInfo#', must contain a valid class-id, a header and a description (exactly two semicolons)"));
          }
          if (theClass == null) {
            System.out.println("LINE: "+line);
            throw new ScriptException(makeError(String.format("Class '%s' not found", classID)));
          }
          String periodName = periodInfo[1];
          String periodDes = periodInfo[2];
          period = makePeriod(theClass, periodName, periodDes, em);

          continue;
        }

        if (!taskTagFound && !line.equals("") && !line.equals(TASKS)) {
          throw new ScriptException(makeError(String.format("Expected a line with: %s",TASKS)));
        } else if (taskTagFound && line.equals(TASKS)) {
          throw new ScriptException(makeError(String.format("Only one tag: %s allowed",TASKS)));
        } else if (line.equals(TASKS)) {
          taskTagFound = true;
        } else {

          String[] td = line.split(SEPARATOR);
          if (td.length != 2) {
            throw new ScriptException(makeError("Only two entries allowed (exactly one semicolon)"));
          }
          String taskName = td[0];
          int maxVal = -1;
          try {
            maxVal = Integer.parseInt(td[1].trim());
          } catch (java.lang.NumberFormatException ex) {
            throw new ScriptException(makeError("Second argument in this line must be a number"));
          }
          makeTask(maxVal, taskName, theClass, period, em);

        }
      }
      em.merge(period);
      em.getTransaction().commit();
      em.getEntityManagerFactory().getCache().evictAll();
    } catch (Exception e) {
      if (em != null) {
        em.getTransaction().rollback();
      }
      if(e instanceof ScriptException){
        throw (ScriptException)e;
      }
      throw new ScriptException(e.getMessage());
    } finally {
      em.close();
    }
  }
}
