package scripthandling;

import entity.SP_Class;
import entity.StudyPointUser;
import entity.UserRole;
import entity.exceptions.ScriptException;
import facade.ClassFacade;
import facade.StudyPointUserFacade;
import java.util.Scanner;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import security.PasswordStorage;

/**
 *
 * @author plaul1
 */
public class ClassAndStudentsStrategy extends ScriptHandler {

  public static final String FRIENDLY_TAG = "_friendlyname_";
  public static final String MAX_POINTS = "_maxpoints_required_";

  protected ClassAndStudentsStrategy(String script, String separator) {
    super(script, separator);
  }

  @Override
  protected void handleScriptTemplate(Scanner scan) throws ScriptException {
    boolean expectsSecondLine = true;
    String friendlyName = null;
    int max = -1;
    int required = -1;
    while (scan.hasNext()) { // Read the first two info entry-lines
      String line = getNextLine(scan);
      if (isLineToSkip(line)) {
        continue;
      }
      //Remember first line is already read
      if (expectsSecondLine) {
        expectsSecondLine = false;
        String[] secondLineParts = line.split(SEPARATOR);
        System.out.println("Separator: " + SEPARATOR.equals("\t") + "---" + secondLineParts.length);
        if (secondLineParts.length != 2) {

          String msg = String.format("Second entry must include (only) the tag %s + its value (separated by a semicolon)", FRIENDLY_TAG);
          throw new ScriptException(makeError(msg));
        }
        if (!secondLineParts[0].equals(FRIENDLY_TAG)) {
          throw new ScriptException(makeError(String.format("Second entry must start with the tag '%s'", FRIENDLY_TAG)));
        }
        friendlyName = secondLineParts[1];
        continue;
      }
      if (!expectsSecondLine) { //Read third line
        String[] thirdLineParts = line.split(SEPARATOR);
        if (thirdLineParts.length != 3) {
          String msg = String.format("Third entry must include the tag '%s' followed by maxPoints and required points (separated by semicolons)", MAX_POINTS);
          throw new ScriptException(makeError(msg));
        }
        if (!thirdLineParts[0].equals(MAX_POINTS)) {
          throw new ScriptException(makeError(String.format("Third entry must include '%s;<maxval>;<requiredval>'", MAX_POINTS)));
        }
        try {
          max = Integer.parseInt(thirdLineParts[1]);
          required = Integer.parseInt(thirdLineParts[2]);
        } catch (NumberFormatException ne) {
          throw new ScriptException(makeError("One or both of the numeric values where NOT numeric"));
        }
        break;
      }
    }
    makeClassAndStudents(scan, friendlyName, max, required);
  }

  private void makeClassAndStudents(Scanner scan, String friendlyName, int maxPoints, int requiredPoints) throws ScriptException {

    boolean classLineExpected = true;
    boolean classCreated = false;
    String className = null;
    int studentCount = 0;
    SP_Class theClass = null;
   
    EntityManager em = emf.createEntityManager();
    em.getTransaction().begin();
    StudyPointUserFacade userFacade = new StudyPointUserFacade();
    try {
      while (scan.hasNext()) {
        String line = getNextLine(scan);
        if (isLineToSkip(line)) {
          continue;
        }
        if (classLineExpected) {
          //This line is Not used, but must be read to get to the next line
          classLineExpected = false;
          continue;
        }

        String[] sd = line.split(SEPARATOR);
        //To make code easier to understand;
        String fullName = sd[0];
        String fName = "";
        String lName = fullName;  //If a name (should not happen) has only one part, it becomes last name
        String[] nameParts = fullName.split(" ");
        if (nameParts.length > 2) {
          for (int i = 0; i < (nameParts.length - 1); i++) {
            fName += " "+nameParts[i];
          }
          lName = nameParts[nameParts.length - 1];
        }

        String userName = sd[3];
        String email = sd[1];

        if (!classCreated) {
          className = sd[2];
          theClass = makeClass(className, friendlyName, maxPoints, requiredPoints, "NOT_USED", em);
          classCreated = true;
        }
        makeStudent(className, userName, fName, lName, email,theClass, em,userFacade);

      }

      em.getTransaction().commit();
    } catch (ScriptException e) {
      System.out.println("ERRRROOOOOOOOOOOOOOOOOOOOR");
      em.getTransaction().rollback();//#######################
      throw e;
    } catch (Exception e) {
      throw new ScriptException(makeError(e.getMessage()));
    } finally {
      
      em.close();
    }
  }

  public SP_Class makeClass(String classId, String nameShort, int maxPointsForSemester, int requredPoints, String description, EntityManager em) throws ScriptException {

//    ClassFacade classFacade = new ClassFacade(emf);
//    SP_Class newClass = classFacade.findSP_Class(classId);
    SP_Class newClass = em.find(SP_Class.class, classId);
    if (newClass != null) {
      throw new ScriptException(makeError(String.format("The class '%s' already exist", newClass.getId())));
    }

    newClass = new SP_Class();
    newClass.setId(classId);
    newClass.setMaxPointForSemester(maxPointsForSemester);
    newClass.setNameShort(nameShort);
    newClass.setRequiredPoints(requredPoints);
    newClass.setSemesterDescription(description);
    try {
      //newClass = classFacade.create(newClass);
      em.persist(newClass);
      
    } catch (Exception e) {
      throw new ScriptException(e.getMessage());
    }

    return newClass;

  }

  public StudyPointUser makeStudent(String classId, String userName, String fName, String lName, String email,
                                    SP_Class theClass, EntityManager em,StudyPointUserFacade userFacade) throws ScriptException {
//    EntityManager em = emf.createEntityManager();
//    em.getTransaction().begin();
    StudyPointUser user = null;

    try {
      Query q = em.createNamedQuery("StudyPointUser.findByUsername", StudyPointUser.class);
      q.setParameter("username", userName);
      user = (StudyPointUser) q.getSingleResult();
        //user = userFacade.findStudyPointUser(userName);
    } catch (NoResultException ex) {
      //User does not already exist (from a previous class), so create him
      user = new StudyPointUser(userName, fName, lName, email, "");
      
      em.persist(user);
      UserRole role = em.find(UserRole.class, "User");
      if (role == null) {
        throw new ScriptException(makeError("Role 'User' not found"));
      }
      role.addStudyPointUser(user);
      user.addRole(role);
      em.merge(role);

    }
    theClass.addStudyPointUser(user);
    user.addClass(theClass);
    em.merge(theClass);
    em.merge(user);
    return user;
  }

}
