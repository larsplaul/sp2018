package scripthandling;

import deploy.DeploymentConfiguration;
import entity.SP_Class;
import entity.SemesterPeriod;
import entity.StudyPoint;
import entity.StudyPointUser;
import entity.Task;
import entity.UserRole;
import entity.exceptions.NonexistentEntityException;
import entity.exceptions.PreexistingEntityException;
import entity.exceptions.ScriptException;
import entity.exceptions.StudyPointException;
import facade.ClassFacade;
import facade.PeriodFacade;
import facade.StudyPointFacade;
import facade.StudyPointUserFacade;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import security.PasswordStorage;

//enum SkipFirstState {
//  CAN_BE_SET,
//  IS_SET,
//  IS_NO_LONGER_VALID
//}

/*
 
NO LONGER USED, check if it can be removed

*/

public class ScriptBuilder {

  //START TOKENS
  public static final String ASSIGN_POINTS = "_AssignPoints_";
  
  
  
  public static final String SKIP_FIRST = "_SKIPFIRST_";

  static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
  private int currentLineNo = 0;

  public SP_Class makeClass(String classId, int maxPointsForSemester, String nameShort, int requredPoints, String description) throws PreexistingEntityException {

    ClassFacade classFacade = new ClassFacade(emf);
    SP_Class newClass = classFacade.findSP_Class(classId);
    if (newClass != null) {
      throw new PreexistingEntityException(String.format("The class '%s' already exist", newClass.getId()));
    }

    newClass = new SP_Class();
    newClass.setId(classId);
    newClass.setMaxPointForSemester(maxPointsForSemester);
    newClass.setNameShort(nameShort);
    newClass.setRequiredPoints(requredPoints);
    newClass.setSemesterDescription(description);
    try {
      newClass = classFacade.create(newClass);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return newClass;

  }

  public StudyPointUser makeStudent(String classId, String userName, String fName, String lName, String email, String phone, String password, String passwordInitial, SP_Class theClass) throws NonexistentEntityException, ScriptException {
    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    StudyPointUser user = null;
    try {
      try {
        Query q = em.createNamedQuery("StudyPointUser.findByUsername", StudyPointUser.class);
        q.setParameter("username", userName);
        user = (StudyPointUser) q.getSingleResult();
      } catch (NoResultException ex) {
        //User does not already exist (from a previous class), so create him
        user = new StudyPointUser(userName, fName, lName, email, phone);
        user.setPasswordInitial(passwordInitial);
        try {
          user.setPassword(password);
        } catch (PasswordStorage.CannotPerformOperationException cpoe) {
          throw new ScriptException(cpoe.getMessage(), cpoe);
        }
        //em.getTransaction().begin();
        em.persist(user);
        UserRole role = em.find(UserRole.class, "User");
        if (role == null) {
          throw new NonexistentEntityException("Role 'User' not found");
        }
        role.addStudyPointUser(user);
        user.addRole(role);
        em.merge(role);
        // em.getTransaction().commit();
      }
      //em.getTransaction().begin();
      theClass.addStudyPointUser(user);
      user.addClass(theClass);
      em.merge(theClass);
      em.merge(user);
      em.getTransaction().commit();
    } finally {
      em.close();
    }
    return user;
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
    if (name.startsWith("#A#")) {
      name = name.replaceFirst("#A#", "");
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

  private void makeClassAndStudents(Scanner scan, String friendlyName, int max, int required) throws PreexistingEntityException, NonexistentEntityException {

    boolean classLineExpected = true;
    boolean classCreated = false;
    String className = null;
    int studentCount = 0;
    SP_Class theClass = null;
    while (scan.hasNext()) {
      if (classLineExpected) {
        String headerLine = getNextLine(scan);
        String[] headers = headerLine.split(";");
        //System.out.println(makeClassRow(headers[0], 240, "sem3-COS",70, "fjlsafjlska"));
        classLineExpected = false;
      } else {
        String studentLine = getNextLine(scan);
        String[] sd = studentLine.split(";");

        if (!classCreated) {
          theClass = makeClass(sd[0], max, friendlyName, required, "NOT_USED");
          classCreated = true;
          className = sd[0];
        }
        try {
          makeStudent(sd[0], sd[1], sd[6], sd[7], sd[3], sd[8], sd[2], sd[2], theClass);
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

  private String getNextLine(Scanner scan) {
    currentLineNo++;
    return scan.nextLine().trim();
  }

  public void createFromScript(String script) throws ScriptException, PreexistingEntityException, NonexistentEntityException {

    try (Scanner scan = new Scanner(script)) {
      String scriptType = getNextLine(scan);
      if (scriptType.equals("#PeriodInfo#")) {
        makeTaskAndStudyPoints(scan);
      } else if (scriptType.equals("#class_and_students#")) {
        String[] secondLine = getNextLine(scan).split("#");
        if (!secondLine[0].equals("friendlyname")) {
          throw new ScriptException(makeError("Second entry in a #class_and_students# file must include a friendly class name"));
        }
        String friendlyName = secondLine[1];
        String[] thirdLine = getNextLine(scan).split("#");
        if (!thirdLine[0].equals("maxpoints_required")) {
          throw new ScriptException(makeError("Third entry in a #class_and_students# file must include 'maxpoints_required#MAX0#REQUIRED'"));
        }
        int max = Integer.parseInt(thirdLine[1]);
        int required = Integer.parseInt(thirdLine[2]);

        makeClassAndStudents(scan, friendlyName, max, required);
        
        
      } else if (scriptType.equals(ASSIGN_POINTS)) {
        assignStudyPoints(scan);
      } else {
        throw new ScriptException(makeError("Script does not include a known script-type in first line"));
      }
    }
  }

  private String makeError(String txt) {

    return String.format("Error (line: %1$d): %2$s", currentLineNo, txt);
  }

  private void makeTaskAndStudyPoints(Scanner scan) throws NonexistentEntityException, ScriptException {

    boolean taskTagFound = false;
    boolean classCreated = false;
    boolean periodCreated = false;

    EntityManager em = emf.createEntityManager();
    try {

      //int lineCount = 0;
      SP_Class theClass = null;
      SemesterPeriod period = null;
      em.getTransaction().begin();
      while (scan.hasNext()) {
        //lineCount++;
        String line = getNextLine(scan);
        if (line.equals("")) {
          continue;
        }
        if (period == null) {
          String[] periodInfo = line.split(";");
          String classID = periodInfo[0];
          theClass = em.find(SP_Class.class, classID);
          if (periodInfo.length != 3) {
            throw new ScriptException(makeError("Line following a '#PeriodInfo#', must contain a valid class-id, a header and a description (exactly two semicolons)"));
          }
          if (theClass == null) {
            throw new NonexistentEntityException(makeError(String.format("Class '%s' not found", classID)));
          }

          String periodName = periodInfo[1];
          String periodDes = periodInfo[2];
          period = makePeriod(theClass, periodName, periodDes, em);

          continue;
        }

        if (!taskTagFound && !line.equals("") && !line.equals("#Tasks#")) {
          throw new ScriptException("Expected a line with: \"#Tasks#\"");
        } else if (taskTagFound && line.equals("#Tasks#")) {
          throw new ScriptException("Only one tag: \"#Tasks#\" allowed");
        } else if (line.equals("#Tasks#")) {
          taskTagFound = true;
        } else {

          String[] td = line.split(";");
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
      //em.flush();
      em.getTransaction().commit();
      em.getEntityManagerFactory().getCache().evictAll();
    } catch (Exception e) {

      if (em != null) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }
  }

  private void assignStudyPoints(Scanner scan) throws ScriptException {
    boolean expectClassAndPeriod = true;
    String classId = null;
    String periodName = null;
    SkipFirstState skipFirst = SkipFirstState.CAN_BE_SET;
    //EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
    EntityManager em = null;
    try {
      while (scan.hasNext()) {
        String line = getNextLine(scan);
        if (line.equals("")) {
          continue;
        }
        if (line.startsWith("#")) {
          continue;
        }
        //Handle second line
        if (expectClassAndPeriod) {
          expectClassAndPeriod = false;
          String[] items = line.split(";");
          if (items.length != 2) {
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
          if (null != skipFirst) switch (skipFirst) {
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
          continue;
        }
        String[] items = line.split(";");
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
            throw new Exception(makeError(String.format("Could not set stydypoints for task '%s', for user '%s'", t.name, userName)));
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
