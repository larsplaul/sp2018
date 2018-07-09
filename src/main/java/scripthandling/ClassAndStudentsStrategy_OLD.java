/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripthandling;

import entity.SP_Class;
import entity.StudyPointUser;
import entity.UserRole;
import entity.exceptions.ScriptException;
import facade.ClassFacade;
import java.util.Scanner;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import security.PasswordStorage;

/**
 *
 * @author plaul1
 */
public class ClassAndStudentsStrategy_OLD extends ScriptHandler {

  public static final String FRIENDLY_TAG = "_friendlyname_";
  public static final String MAX_POINTS = "_maxpoints_required_";

  protected ClassAndStudentsStrategy_OLD(String script,String separator) {
    super(script,separator);
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
        System.out.println("Separator: "+SEPARATOR.equals("\t")+"---"+secondLineParts.length );
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
      String userName = sd[1];
      String fName= sd[6];
      String lName= sd[7];
      String email = sd[3];
      String phone = sd[8];
      String pw = sd[2];

      if (!classCreated) {
        className = sd[0];
        theClass = makeClass(className, friendlyName,maxPoints, requiredPoints, "NOT_USED");
        classCreated = true;
      }
      try {
        makeStudent(className, userName, fName, lName, email, phone, pw, pw, theClass);
      } catch (ScriptException e) {
        throw e;
      } catch(Exception e){
        throw new ScriptException(makeError(e.getMessage()));
      }
    }
  }

  public SP_Class makeClass(String classId,String nameShort, int maxPointsForSemester,  int requredPoints, String description) throws ScriptException {

    ClassFacade classFacade = new ClassFacade(emf);
    SP_Class newClass = classFacade.findSP_Class(classId);
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
      newClass = classFacade.create(newClass);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return newClass;

  }

  public StudyPointUser makeStudent(String classId, String userName, String fName, String lName, String email, String phone, String password, String passwordInitial, SP_Class theClass) throws ScriptException {
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
          throw new ScriptException(cpoe.getMessage());
        }
        //em.getTransaction().begin();
        em.persist(user);
        UserRole role = em.find(UserRole.class, "User");
        if (role == null) {
          throw new ScriptException(makeError("Role 'User' not found"));
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
}
