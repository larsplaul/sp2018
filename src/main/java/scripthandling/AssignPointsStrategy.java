package scripthandling;

import entity.SP_Class;
import entity.StudyPointUser;
import entity.exceptions.ScriptException;
import entity.exceptions.StudyPointException;
import facade.ClassFacade;
import facade.PeriodFacade;
import facade.StudyPointFacade;
import facade.StudyPointUserFacade;
import java.util.Arrays;
import java.util.Scanner;
import javax.persistence.EntityManager;

enum SkipFirstState {
  CAN_BE_SET,
  IS_SET,
  IS_NO_LONGER_VALID
}

/**
 *
 * @author plaul1
 */
public class AssignPointsStrategy extends ScriptHandler {

  public static final String SKIP_FIRST = "_SKIPFIRST_";

  protected AssignPointsStrategy(String script, String separator) {
    super(script,separator);
  }

  @Override
  protected void handleScriptTemplate(Scanner scan) throws ScriptException {

    boolean expectClassAndPeriod = true;
    String classId = null;
    String periodName = null;
    SkipFirstState skipFirst = SkipFirstState.CAN_BE_SET;
    //EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    EntityManager em = null;
    try {
      while (scan.hasNext()) {
        String line = getNextLine(scan);
        if (isLineToSkip(line)) {
          continue;
        }
        //Handle second line
        if (expectClassAndPeriod) {
          expectClassAndPeriod = false;
          String[] items = line.split(SEPARATOR);
          if (items.length != 2) {
            System.out.println("LINE WAS: " + line);
            throw new Exception(makeError("Exactly two entries must follow the Script type (Class ; Period)"));
          }
          classId = items[0].trim();
          periodName = items[1].trim();

          //Check if class exist and if period exists in the class, for better error reporting
          SP_Class theClass = new ClassFacade((emf)).findSP_Class(classId);
          if (theClass == null) {
            throw new Exception(makeError(String.format("Class '%s' not found", classId)));
          }
          try {
            new PeriodFacade().getPeriodFromClassAndDescription(classId, periodName);
          } catch (Exception e) {
            throw new Exception(makeError(String.format("Period '%s' not found in class '%s", periodName, classId)));
          }
          continue;
        }
        //Handle remaining lines (all the studpoint assignments)
        if (line.equals(SKIP_FIRST)) {
          if (null != skipFirst) {
            switch (skipFirst) {
              case CAN_BE_SET:
                skipFirst = SkipFirstState.IS_SET;
                break;
              case IS_SET:
                throw new Exception(makeError(String.format("%s, can only be set once (as the third entry-line)", SKIP_FIRST)));
              case IS_NO_LONGER_VALID:
                throw new Exception(makeError(String.format("%s, can only be set as the third entry-lin (after Class;period, and before points)", SKIP_FIRST)));
              default:
                break;
            }
          }
          continue;
        }
        String[] items = line.split(SEPARATOR);
        if (skipFirst == SkipFirstState.IS_SET) {
          if (items.length < 4 || items.length % 2 != 0) {
            throw new Exception(makeError("Line must start with a studentName (will be skipped), studentId, followed by (minumum) one task name followed by the points to assign for the task"));
          }
          items = Arrays.copyOfRange(items, 1, items.length); //SKIP_FIRST
        } else if (items.length < 3 || items.length % 2 == 0) {
          throw new Exception(makeError("Line must start with a studentId, followed by (minumum) one task name followed by the points to assign for the task"));
        }
        skipFirst = skipFirst == skipFirst.IS_SET ? skipFirst.IS_SET : skipFirst.IS_NO_LONGER_VALID;
        if (em == null) {
          em = emf.createEntityManager();
          em.getTransaction().begin();
          System.out.println("Started a Transaction");
        }
        String userName = items[0].trim();
        StudyPointUser user;
        try {
          user = new StudyPointUserFacade().getUserFromUserNameIfInClass(userName, classId);
        } catch (StudyPointException ex) {
          throw new Exception(makeError(ex.getMessage()));
        }
        String[] rest = Arrays.copyOfRange(items, 1, items.length);
        TaskPair t = null;
        for (int i = 0; i < rest.length;) {
          try {
            int score = Integer.parseInt(rest[i + 1].trim());
            t = new TaskPair(rest[i].trim(), score);
            StudyPointFacade.setStudyPoint(em, t.score, userName, t.name, periodName, classId);
            i += 2;
          } catch (NumberFormatException ne) {
            throw new Exception(makeError(String.format("%s is not a number (studypoint score)", rest[i + 1])));
          } catch (Exception ex) {
            if (ex instanceof StudyPointException) {
              throw new StudyPointException(makeError(ex.getMessage()));
            }
            String name = t != null ? t.name : "-?-";
            throw new Exception(makeError(String.format("Could not set stydypoints for task '%s', for user '%s'", name, userName)));
          }
        }
      }
    } catch (Exception ex) {
      System.out.println("UPPPPPPPS");
      if (em != null) {
        em.getTransaction().rollback();
        em.close();
      }
     
      throw new ScriptException(ex.getMessage());
    }
    if (em != null) {
      em.getTransaction().commit();
      em.close();
    }
  }

}
