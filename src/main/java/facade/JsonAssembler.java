package facade;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import deploy.DeploymentConfiguration;
import entity.SP_Class;
import entity.SemesterPeriod;
import entity.StudyPoint;
import entity.StudyPointUser;
import entity.Task;
import entity.UserRole;
import entity.exceptions.NonexistentEntityException;
import entity.exceptions.PreexistingEntityException;
import entity.exceptions.StudyPointException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import security.PasswordStorage;

public class JsonAssembler {

  EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
  //private static final Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

  public EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  public String getAllClasses() {
    ClassFacade classFacade = new ClassFacade(emf);
    List<SP_Class> classes = classFacade.findSP_ClassEntities();
    return makeJsonForClasses(classes);
  }

  public String getUser(String userName) {
    EntityManager em = getEntityManager();
    try {
      StudyPointUser user = getUserFromUserName(em, userName);
      return gson.toJson(makeJsonUser(user));
    } finally {
      em.close();
    }
  }

  private StudyPointUser getUserFromUserName(EntityManager em, String userName) {
    Query query = em.createNamedQuery("StudyPointUser.findByUsername", StudyPointUser.class);
    query.setParameter("username", userName);
    StudyPointUser user = (StudyPointUser) query.getSingleResult();
    return user;
  }

  public String getAllUsers() {
    StudyPointUserFacade userFacade = new StudyPointUserFacade(emf);
    List<StudyPointUser> allUsers = userFacade.findStudyPointUserEntities();
    JsonArray jsonStudents = new JsonArray();
    for (StudyPointUser user : allUsers) {
      JsonObject userAssJson = makeJsonUser(user);
      jsonStudents.add(userAssJson);
    }
    return gson.toJson(jsonStudents);
  }

  private JsonObject makeJsonUser(StudyPointUser user) {
    JsonObject userAssJson = new JsonObject();
    userAssJson.addProperty("_id", user.getId());
    userAssJson.addProperty("firstName", user.getFirstName());
    userAssJson.addProperty("lastName", user.getLastName());
    userAssJson.addProperty("phone", user.getPhone());
    userAssJson.addProperty("email", user.getEmail());
    userAssJson.addProperty("user", user.getUserName());
    String roleName = user.getRoles().size() > 0 ? user.getRoles().get(0).getRoleName() : "";
    userAssJson.addProperty("role", roleName);
    JsonArray usersClasses = new JsonArray();
    for (SP_Class spClass : user.get_Classes()) {
      usersClasses.add(new JsonPrimitive(spClass.getId()));
    }
    userAssJson.add("classes", usersClasses);
    return userAssJson;
  }

  /*
   Get all classes for this student
   */
  public String getClassesForStudent(int studentId) {
    EntityManager em = getEntityManager();
    try {
      List<SP_Class> classes = em.find(StudyPointUser.class, studentId).get_Classes();
      return makeJsonForClasses(classes);
    } finally {
      em.close();
    }
  }

  public String getClass(String id) {
    ClassFacade classFacade = new ClassFacade(emf);
    SP_Class c = classFacade.findSP_Class(id);
    JsonObject classAsJson = new JsonObject();
    classAsJson.addProperty("_id", c.getId());
    classAsJson.addProperty("nameShort", c.getNameShort());
    classAsJson.addProperty("description", c.getSemesterDescription());
    classAsJson.addProperty("maxPointForSemester", c.getMaxPointForSemester());
    classAsJson.addProperty("requiredPoints", c.getRequiredPoints());
    JsonArray jsonPeriods = new JsonArray();
    for (SemesterPeriod p : c.getPeriods()) {
      JsonObject jsonPeriod = new JsonObject();
      jsonPeriod.addProperty("_id", p.getId());
      jsonPeriod.addProperty("periodName", p.getPeriodName());
      jsonPeriods.add(jsonPeriod);
    }
    classAsJson.add("periods", jsonPeriods);
    return gson.toJson(classAsJson);
  }

  public String getFullClassInfo(String id) {
    ClassFacade classFacade = new ClassFacade(emf);
    SP_Class c = classFacade.findSP_Class(id);
    JsonObject classAsJson = new JsonObject();
    classAsJson.addProperty("id", c.getId());
    classAsJson.addProperty("nameShort", c.getNameShort());
    classAsJson.addProperty("description", c.getSemesterDescription());
    classAsJson.addProperty("maxPointForSemester", c.getMaxPointForSemester());
    classAsJson.addProperty("requiredPoints", c.getRequiredPoints());
    JsonArray jsonStudents = new JsonArray();
    for (StudyPointUser user : c.getUsers()) {
      JsonObject jsonUser = new JsonObject();
      jsonUser.addProperty("id", user.getId());
      jsonUser.addProperty("firstName", user.getFirstName());
      jsonUser.addProperty("lastName", user.getLastName());
      jsonStudents.add(jsonUser);
    }
    classAsJson.add("students", jsonStudents);

    JsonArray jsonPeriods = new JsonArray();
    for (SemesterPeriod p : c.getPeriods()) {
      JsonObject jsonPeriod = new JsonObject();
      jsonPeriod.addProperty("_id", p.getId());
      jsonPeriod.addProperty("periodName", p.getPeriodName());
      jsonPeriods.add(jsonPeriod);
    }
    classAsJson.add("periods", jsonPeriods);
    return gson.toJson(classAsJson);
  }

  public String getStudyPointsForStudent(String classId, int studentId,boolean isValidIp) {
    ClassFacade classFacade = new ClassFacade(emf);
    SP_Class c = classFacade.findSP_Class(classId);
    StudyPointUserFacade spFacade = new StudyPointUserFacade(emf);
    StudyPointUser user = spFacade.findStudyPointUser(studentId);

    JsonObject classAsJson = new JsonObject();
    classAsJson.addProperty("maxPointForSemester", c.getMaxPointForSemester());
    classAsJson.addProperty("requiredPoints", c.getRequiredPoints());
    classAsJson.addProperty("studentName", user.getFirstName() + " " + user.getLastName());
    classAsJson.addProperty("user", user.getUserName());
    JsonArray jsonPeriods = new JsonArray();
    for (SemesterPeriod p : c.getPeriods()) {
      JsonObject jsonPeriod = new JsonObject();
      //jsonPeriod.addProperty("_id", p.getId());
      jsonPeriod.addProperty("periodName", p.getPeriodName());
      jsonPeriod.addProperty("periodDescription", p.getPeriodDescription());
      JsonArray jsonTasks = new JsonArray();
      java.util.Date now = new java.util.Date();
      for (Task task : p.getTasks()) {
        JsonObject jsonTask = new JsonObject();
        jsonTask.addProperty("name", task.getName());
        jsonTask.addProperty("maxScore", task.getMaxScore());
        jsonTask.addProperty("score", task.getStudyPointForStudent(studentId).getScore());
        //jsonTask.addProperty("studyPointId", task.getStudyPointForStudent(studentId).getId());
        jsonTask.addProperty("spId", task.getStudyPointForStudent(studentId).getId());
        if (task.hasValidCode(now) && isValidIp) {
           jsonTask.addProperty("auto", 1); //Value of auto 1: no need to provide code, 0 - code must be provided (un-approved IP)
           //jsonTask.addProperty("spId", task.getStudyPointForStudent(studentId).getId());
        }
   /*   Replace the statements above with below, to add a response, 
        which the client will use to add an input field to write "todays code" if not at school*/
//        if (task.hasValidCode(now)) {
//           jsonTask.addProperty("auto", isValidIp? 1:0); //Value of auto 1: no need to provide code, 0 code must be provided (un-approved IP)
//        }
        jsonTasks.add(jsonTask);
      }
      jsonPeriod.add("tasks", jsonTasks);
      jsonPeriods.add(jsonPeriod);
    }
    classAsJson.add("periods", jsonPeriods);

    return gson.toJson(classAsJson);
  }

  public String getAutoLoginCode(int taskId) throws NonexistentEntityException, StudyPointException {
    String code = TaskFacade.createTaskCode(taskId);
    JsonObject json = new JsonObject();
    json.addProperty("code", code);
    return gson.toJson(json);
  }

  /*
   Returns a JSON structure contaning 
   Used for the handout studypoints for a class/period
   1) Information about the requested Period, including the owner class
   2) All tasks for the requested Period
   3) All students for the requeste Period, and for each student a list of his studypoints for this period 
   */
  public String getPeriod(int id) {
    SemesterPeriodFacade facade = new SemesterPeriodFacade(emf);
    SemesterPeriod sp = facade.findSemesterPeriod(id);
    JsonObject periodJson = new JsonObject();
    periodJson.addProperty("id", sp.getId());
    periodJson.addProperty("classId", sp.getInClass().getId());
    periodJson.addProperty("periodName", sp.getPeriodName());
    periodJson.addProperty("periodDescription", sp.getPeriodDescription());

    JsonArray jsonTasks = new JsonArray();
    for (Task t : sp.getTasks()) {
      JsonObject jsonTask = new JsonObject();
      jsonTask.addProperty("name", t.getName());
      jsonTask.addProperty("maxScore", t.getMaxScore());
      if (t.isAllowAutoAttendance()) {
        jsonTask.addProperty("id", t.getId());
      }
      //jsonTask.addProperty("id", t.getId()); TODO: figure out whether id is nessecary
      jsonTasks.add(jsonTask);
    }

    periodJson.add("tasks", jsonTasks);

    List<StudyPointUser> users = sp.getInClass().getUsers();
    JsonArray jsonUsers = new JsonArray();
    for (StudyPointUser user : users) {
      JsonObject jsonUser = new JsonObject();
      jsonUser.addProperty("userId", user.getId());
      jsonUser.addProperty("userId", user.getId());
      jsonUser.addProperty("fullName", user.getFullName());

      JsonArray jsonStudyPoints = new JsonArray();
      for (StudyPoint studyPoint : user.getStudyPoints(id)) {
        JsonObject jsonStudyPoint = new JsonObject();
        jsonStudyPoint.addProperty("id", studyPoint.getId());
        jsonStudyPoint.addProperty("forTask", studyPoint.getTask().getName());
        jsonStudyPoint.addProperty("maxScore", studyPoint.getTask().getMaxScore());
        jsonStudyPoint.addProperty("score", studyPoint.getScore());
        //jsonStudyPoint.addProperty("userId", studyPoint.getStudyPointUser().getId());//TODO Remove me
        jsonStudyPoints.add(jsonStudyPoint);
      }
      jsonUser.add("scores", jsonStudyPoints);

      jsonUsers.add(jsonUser);
    }

    periodJson.add("students", jsonUsers);
    return gson.toJson(periodJson);
  }

  public String editStudyPoints(String jsonAsString) {
    StudyPointFacade studyPointFacade = new StudyPointFacade(emf);
    JsonArray scores = new JsonParser().parse(jsonAsString).getAsJsonArray();
    for (JsonElement s : scores) {
      int id = s.getAsJsonObject().get("id").getAsInt();
      int score = s.getAsJsonObject().get("score").getAsInt();
      //System.out.println(s.getAsJsonObject().get("score").getAsInt());
      //System.out.println("" + id + ", " + score);
      try {
        studyPointFacade.edit(id, score);
      } catch (Exception ex) {
        Logger.getLogger(JsonAssembler.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return "Todo";
  }

  public String removeStudentsFromClass(String jsonAsString) {
    JsonObject jsonClass = new JsonParser().parse(jsonAsString).getAsJsonObject();
    String classId = jsonClass.get("id").getAsString();
    String nameShort = jsonClass.get("nameShort").getAsString();
    int requiredPoints = jsonClass.get("requiredPoints").getAsInt();
    int maxPointForSemester = jsonClass.get("maxPointForSemester").getAsInt();
    JsonArray studentsToRemoveJson = jsonClass.getAsJsonArray("studentsToRemove");
    List<Integer> studentsToRemove = new ArrayList();
    for (JsonElement studentJson : studentsToRemoveJson) {
      //studentsToRemove.add(studentJson.getAsInt());
      int id = studentJson.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
      studentsToRemove.add(id);
    }
    EntityManager em = getEntityManager();
    try {
      SP_Class theClass = em.find(SP_Class.class, classId);
      em.getTransaction().begin();
      for (Iterator<StudyPointUser> iter = theClass.getUsers().listIterator(); iter.hasNext();) {
        StudyPointUser user = iter.next();
        for (int idToRemove : studentsToRemove) {
          if (user.getId() == idToRemove) {
            //Note: studypoint earned by student in this class is not removed to preserve history                     
            user.get_Classes().remove(theClass);
            em.merge(user);
            iter.remove();
          }
        }
      }
      theClass.setNameShort(nameShort);
      theClass.setMaxPointForSemester(maxPointForSemester);
      theClass.setRequiredPoints(requiredPoints);
      em.merge(theClass);
      em.getTransaction().commit();
    } finally {
      em.close();
    }

    return "todo";
  }

  public String makeUser(String userAsJson) throws PreexistingEntityException {
    JsonObject jsonClass = new JsonParser().parse(userAsJson).getAsJsonObject();
    String userName = jsonClass.get("user").getAsString();
    String fName = jsonClass.get("firstName").getAsString();
    String lName = jsonClass.get("lastName").getAsString();
    String phone = jsonClass.get("phone").getAsString();
    String role = jsonClass.get("role").getAsString();
    String email = jsonClass.get("email").getAsString();
    JsonArray classesJson = jsonClass.getAsJsonArray("classes");
    List<String> classes = new ArrayList();
    if (classesJson != null) {
      for (JsonElement classJson : classesJson) {
        classes.add(classJson.getAsString());
      }
    }
    StudyPointUser user = new StudyPointUser(userName, fName, lName, email, phone);
    EntityManager em = emf.createEntityManager();
    checkUniqueProperties(em, "userName", userName);
    checkUniqueProperties(em, "email", email);
    em.getTransaction().begin();
    for (String classId : classes) {
      SP_Class spClass = em.find(SP_Class.class, classId);
      //create default StudyPoints for existing tasks
      for (SemesterPeriod period : spClass.getPeriods()) {
        for (Task task : period.getTasks()) {
          StudyPoint sp = new StudyPoint(task, user);
          task.addStudyPoint(sp);
          em.merge(task);
          user.addStudyPoint(sp);
        }
      }
    }
    em.persist(user);

    for (String classId : classes) {
      SP_Class spClass = em.find(SP_Class.class, classId);
      user.addClass(spClass);
      spClass.addStudyPointUser(user);
      em.merge(spClass);
    }
    UserRole userRole = em.find(UserRole.class, role);
    userRole.addStudyPointUser(user);
    user.addRole(userRole);
    em.merge(userRole);
    user = em.merge(user); //Should get the user with the autogenerated id
    em.flush();
    em.getTransaction().commit();
    em.close();
    return gson.toJson(makeJsonUser(user));
  }

  public String resetPassword(int id) throws PasswordStorage.CannotPerformOperationException {
    EntityManager em = getEntityManager();
    try {
      StudyPointUser user = em.find(StudyPointUser.class, id);
      em.getTransaction().begin();
      user.setPassword(user.getPasswordInitial());
      em.merge(user);
      em.getTransaction().commit();
      return "{\"info\":\"Password was reset\"}";

    } finally {
      em.close();
    }
  }

  public String getTotalStudyPointsForSemester(String classId) {
    ClassFacade classFacade = new ClassFacade(emf);
    SP_Class c = classFacade.findSP_Class(classId);
    JsonObject classAsJson = new JsonObject();
    classAsJson.addProperty("_id", c.getId());
    classAsJson.addProperty("nameShort", c.getNameShort());
    classAsJson.addProperty("description", c.getSemesterDescription());
    classAsJson.addProperty("maxPointForSemester", c.getMaxPointForSemester());
    classAsJson.addProperty("requiredPoints", c.getRequiredPoints());
    JsonArray jsonStudents = new JsonArray();
    for (StudyPointUser user : c.getUsers()) {
      int pointsEarnedInThisClass = 0;
      for (StudyPoint sp : user.getStudyPoints()) {
        if (sp.getTask().getSemesterPeriod().getInClass().getId().equals(classId)) {
          pointsEarnedInThisClass += sp.getScore();
        }
      }

      JsonObject jsonUser = new JsonObject();
      jsonUser.addProperty("id", user.getId());
      jsonUser.addProperty("user", user.getUserName());
      jsonUser.addProperty("name", user.getFirstName() + " " + user.getLastName());
      jsonUser.addProperty("points", pointsEarnedInThisClass);
      jsonStudents.add(jsonUser);
    }
    classAsJson.add("Students", jsonStudents);

    return gson.toJson(classAsJson);
  }

  public String editPassword(String passwordInfo, String userPrincipal) throws PasswordStorage.CannotPerformOperationException, PasswordStorage.InvalidHashException, AuthenticationException {
    EntityManager em = getEntityManager();
    try {
      StudyPointUser user = getUserFromUserName(em, userPrincipal);
      if (user == null) {
        //todo
      }
      JsonObject passwordInfoJson = new JsonParser().parse(passwordInfo).getAsJsonObject();
      String old = passwordInfoJson.get("old").getAsString();
      String newPw1 = passwordInfoJson.get("newPw1").getAsString();
      String newPw2 = passwordInfoJson.get("newPw2").getAsString();

      if (!security.PasswordStorage.verifyPassword(old, user.getPassword())) {
        throw new SecurityException("You did not provide the right original password");
      }
      if (!newPw1.equals(newPw2)) {
        throw new SecurityException("New password and the 'retyped' value does not match");
      }
      em.getTransaction().begin();
      user.setPassword(newPw1);
      em.merge(user);
      em.getTransaction().commit();
      return "{\"info\": \"Password was changed \"}";
    } finally {
      em.close();
    }
  }

  /*
   When a user is editing "himself" he can only change name, email and phone, and only for himnself --> we get this user via the UserPrincipal
   */
  public String editUser(String userAsJson, String userPrincipal) throws PreexistingEntityException, NonexistentEntityException {
    JsonObject jsonClass = new JsonParser().parse(userAsJson).getAsJsonObject();

    String userName = null;
    String role = null;
    List<String> newClasses = null;
    int id = jsonClass.get("_id").getAsInt();
    String fName = jsonClass.get("firstName").getAsString();
    String lName = jsonClass.get("lastName").getAsString();
    String phone = jsonClass.get("phone").getAsString();
    String email = jsonClass.get("email").getAsString();
    if (userPrincipal == null) {
      userName = jsonClass.get("user").getAsString();
      role = jsonClass.get("role").getAsString();
      JsonArray classesJson = jsonClass.getAsJsonArray("classes");
      newClasses = new ArrayList();
      if (classesJson != null) {
        for (JsonElement classJson : classesJson) {
          newClasses.add(classJson.getAsString());
        }
      }
    }
    EntityManager em = emf.createEntityManager();
    try {

      StudyPointUser user = null;
      if (userPrincipal != null) {
        user = getUserFromUserName(em, userPrincipal);
      } else {
        user = em.find(StudyPointUser.class, id);
      }
      boolean isAdmin = user.getRoles().get(0).getRoleName().equals("Admin");
      if (userPrincipal != null && !isAdmin) {
        if (!user.getFirstName().equals(fName) || !user.getLastName().equals(lName)) {
          throw new SecurityException("You are not authorized to change name values");
        }
      }
      user.setEmail(email);
      user.setFirstName(fName);
      user.setLastName(lName);
      user.setPhone(phone);
      em.getTransaction().begin();
      if (userPrincipal == null) {
        if (user == null) {
          throw new NonexistentEntityException(String.format("User with id (%s) not found", id));
        }
        if (!user.getUserName().equals(userName)) {
          checkUniqueProperties(em, "userName", userName);
        }
        if (!user.getEmail().equals(email)) {
          checkUniqueProperties(em, "email", email);
        }

        user.setUserName(userName);
        List<String> classesToAdd = new ArrayList();

        removeClassesAndStudyPoints(newClasses, user, em);
        addClassesAndStudyPoints(newClasses, classesToAdd, em, user);
      }
      user = em.merge(user); //Should get the user with the autogenerated id
      em.flush();
      em.getTransaction().commit();
      return gson.toJson(makeJsonUser(user));
    } finally {
      em.close();
    }
  }

  private void addClassesAndStudyPoints(List<String> newClasses, List<String> classesToAdd, EntityManager em, StudyPointUser user) {
    //Find new classes to add
    for (String newClassId : newClasses) {
      boolean found = false;
      for (SP_Class existingClass : user.get_Classes()) {
        if (newClassId.equals(existingClass.getId())) {
          found = true;
        }
      }
      if (!found) {
        classesToAdd.add(newClassId);
      }

    }

    for (String classId : classesToAdd) {
      SP_Class spClass = em.find(SP_Class.class, classId);
      //create default StudyPoints for existing tasks
      for (SemesterPeriod period : spClass.getPeriods()) {
        for (Task task : period.getTasks()) {
          StudyPoint sp = new StudyPoint(task, user);
          task.addStudyPoint(sp);
          em.merge(task);
          user.addStudyPoint(sp);
          em.persist(sp);
//          em.flush();
//          System.out.println(""+sp.getId()+ ", " +sp.getTask().getName()+" : "+sp.getTask().getSemesterPeriod().getPeriodName());
        }
      }
    }
    for (String classId : classesToAdd) {
      SP_Class spClass = em.find(SP_Class.class, classId);
      user.addClass(spClass);
      spClass.addStudyPointUser(user);
      em.merge(spClass);
    }
  }

  /*
   Remove thoses classes from existing classes not found in the list "newClasses".
   Also remove all studypoints (theese will be lost --> perhaps not the right solution) belonging to the user and class being removed
   */
  private void removeClassesAndStudyPoints(List<String> newClasses, StudyPointUser user, EntityManager em) {
    //Find classes to remove
    List<SP_Class> classesToRemove = new ArrayList();
    for (SP_Class exClass : user.get_Classes()) {
      boolean found = false;
      for (String classId : newClasses) {
        if (classId.equals(exClass.getId())) {
          found = true;
        }
      }
      if (found == false) {
        classesToRemove.add(exClass);
        //Delete all studypoint for the student related to the class being removed
        //Map<Task,List<StudyPoint>> studyPointsToRemoveFromTask = new HashMap();

        for (SemesterPeriod p : exClass.getPeriods()) {
          for (Task t : p.getTasks()) {
            //List<StudyPoint> studyPointsToRemove = new ArrayList();
            for (StudyPoint sp : t.getStudyPoints()) {
              if (Objects.equals(sp.getStudyPointUser().getId(), user.getId())) {
                user.getStudyPoints().remove(sp);
                em.remove(sp);
              }
            }
          }
        }
      }
    }
    for (SP_Class classToRemove : classesToRemove) {
      // This HAS to happen outside the itereation to avoid a java.util.ConcurrentModificationException
      classToRemove.getUsers().remove(user);//Remove user from class
      user.get_Classes().remove(classToRemove);//Remove class from user
      em.merge(classToRemove);
    }
  }

  private void checkUniqueProperties(EntityManager em, String property, String value) throws PreexistingEntityException {
    Query q = null;
    q = em.createQuery(String.format("SELECT user FROM StudyPointUser user where user.%s = :value", property));
    q.setParameter("value", value);

    try {
      if (q.getSingleResult() != null) {
        throw new PreexistingEntityException(String.format("%1s %2s is taken", property, value));
      }
    } catch (NoResultException ex) {
      //This is what we would hope for
    }
  }

//  public String makeUser(String userAsJson){
//    JsonObject jsonClass = new JsonParser().parse(userAsJson).getAsJsonObject();
//    String userName = jsonClass.get("user").getAsString();
//    String fName = jsonClass.get("firstName").getAsString();
//    String lName = jsonClass.get("lastName").getAsString();
//    String phone = jsonClass.get("phone").getAsString();
//    String role = jsonClass.get("role").getAsString();
//    String email = jsonClass.get("email").getAsString();
//    JsonArray classesJson = jsonClass.getAsJsonArray("classes");
//    List<String> classes = new ArrayList();
//    for (JsonElement classJson : classesJson) {
//      classes.add(classJson.getAsString());
//    }
//    StudyPointUser user = new StudyPointUser(userName,fName,lName,email,phone);
//    //ClassFacade classFacade = new ClassFacade(emf);
//    EntityManager em = emf.createEntityManager();
//    em.getTransaction().begin();
//    for(String classId : classes){
//      //SP_Class spClass =classFacade.findSP_Class(classId);
//      SP_Class spClass =em.find(SP_Class.class,classId);
//      user.addClass(spClass);
//      spClass.addStudyPointUser(user);
//      em.merge(spClass);
//      //create default StudyPoints for existing tasks
//      for(SemesterPeriod period: spClass.getPeriods()){
//        for(Task task : period.getTasks()){
//          StudyPoint sp = new StudyPoint(task,user);
//          task.addStudyPoint(sp);
//          em.merge(task);
//          user.addStudyPoint(sp);
//        }
//      }
//      
//      
//    }
//    
//    UserRole userRole = em.getReference(UserRole.class, role);
//    //em.close();
//    user.addRole(userRole);
//    
////    StudyPointUserFacade userFacade = new StudyPointUserFacade(emf);
////    userFacade.create(user);
//    //em.getTransaction().begin();
//    
//    em.persist(user);
//    
//    em.getTransaction().commit();
//    em.close();
//    return "{\"Todo\": \"Todo\"";
//  }
//  
  public String deleteUser(int id) {
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      StudyPointUser user = em.find(StudyPointUser.class, id);

      //Object References must be updated, otherwise changes requires a restart of the server
      for (SP_Class aClass : user.get_Classes()) {
        StudyPointUser userToRemove = null;
        for (StudyPointUser u : aClass.getUsers()) {
          if (u.getId() == id) {
            userToRemove = u;
            break;
          }
        }
        aClass.getUsers().remove(userToRemove);
        em.merge(aClass);
      }
      em.remove(user);
      em.getTransaction().commit();

      return gson.toJson(makeJsonUser(user));
    } finally {
      em.close();
    }
  }

  // ####################################################################
  //Methods for the User Role
  // ####################################################################
  public String getClassesForCurrentUser(String userName) {

    EntityManager em = getEntityManager();
    try {
      StudyPointUser user = getUserFromUserName(em, userName);
      List<SP_Class> classes = user.get_Classes();
      return makeJsonForClasses(classes);
    } finally {
      em.close();
    }
  }

  public String getStudyPointsForCurrentUser(String classId, String userName,boolean isValidIP) {

    EntityManager em = getEntityManager();
    try {
      StudyPointUser user = getUserFromUserName(em, userName);
      return getStudyPointsForStudent(classId, user.getId(),isValidIP);
    } finally {
      em.close();
    }
  }
  
  public void registerAttendance(String userName, int taskId, int userId, String code)  throws StudyPointException {

    
    EntityManager em = getEntityManager();
    try {
      StudyPointUser user = getUserFromUserName(em, userName);
      TaskFacade.registerAttendance(user.getId(), taskId, userId,code);
    } finally {
      em.close();
    }
  }

  /*Private Methods */
  private String makeJsonForClasses(List<SP_Class> classes) {
    JsonArray jsonClasses = new JsonArray();
    for (SP_Class c : classes) {
      JsonObject jo = new JsonObject();
      jo.addProperty("_id", c.getId());
      jo.addProperty("nameShort", c.getNameShort());
      jsonClasses.add(jo);
    }
    String jsonStr = gson.toJson(jsonClasses);
    return jsonStr;
  }
}
