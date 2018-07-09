package scripthandling;

import deploy.DeploymentConfiguration;
import entity.exceptions.NonexistentEntityException;
import entity.exceptions.PreexistingEntityException;
import entity.exceptions.ScriptException;
import java.util.Scanner;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


/**
 *
 * @author plaul1
 */
public abstract class ScriptHandler {

  //START TOKENS
  public static final String ASSIGN_POINTS = "_AssignPoints_";
  public static final String PERIOD_INFO = "_PeriodInfo_";
  public static final String CLASS_AND_STUDENTS = "_class_and_students_";
  public static final String ADD_VALID_IPS = "_add_valid_ips_";  
  
  
  protected String SEPARATOR = ";";

  static EntityManagerFactory emf = Persistence.createEntityManagerFactory(DeploymentConfiguration.PU_NAME);
  protected int currentLineNo = 0;

  protected String getNextLine(Scanner scan) {
    currentLineNo++;
    return scan.nextLine().trim();
  }

  protected boolean isLineToSkip(String line) {
    if (line.equals("")) {
      return true;
    }
    if (line.startsWith("#")) {
      return true;
    }
    return false;
  }

  static String makeError(int lineNo, String txt) {
    return String.format("Error (line: %1$d): %2$s", lineNo, txt);
  }

  protected String makeError(String txt) {
    return String.format("Error (line: %1$d): %2$s", currentLineNo, txt);
  }

  protected String script;

  protected ScriptHandler(String script, String sep) {
    this.script = script;
    this.SEPARATOR = sep;
  }

  protected abstract void handleScriptTemplate(Scanner scan) throws ScriptException;

  public void executeScript() throws ScriptException {
    try (Scanner scan = new Scanner(script)) {
      getNextLine(scan); //First line, with script type is read by MakeScriptHandler
      handleScriptTemplate(scan);
    }
  }
  
  public static ScriptHandler MakeScriptHandler(String script) throws ScriptException, PreexistingEntityException, NonexistentEntityException {

    try (Scanner scan = new Scanner(script)) {
      String firstLine = scan.nextLine().trim();
      String[] firstLineParts = firstLine.split(";");
      String scriptType = firstLineParts[0];
      String sep = ";"; //Default if no value is provide for separator
      if(firstLineParts.length > 2){
        throw new ScriptException(makeError(1, "Max two entries (one semicolon) allowed in line 1"));
      }
      if(firstLineParts.length ==2){
        String sepFound = firstLineParts[1];
        
        switch (sepFound) {
          case "TAB":
            sep = "\t";
            break;
          case "SEMICOLON":
            sep = ";";
            break;
          case "COMMA":
            sep = ",";
            break;
          default:
            throw new ScriptException(makeError(1, "Only 'TAB', 'SEMICOLON', or 'COMMA' are allowed values as separator "));
        }
        
      }
      switch (scriptType) {
        case PERIOD_INFO:
          return new PeriodInfoStrategy(script,sep);
        case CLASS_AND_STUDENTS:
          return new ClassAndStudentsStrategy(script,sep);
        case ASSIGN_POINTS:
          return new AssignPointsStrategy(script,sep);
        case ADD_VALID_IPS:
          return new ValidIpsStrategy(script,sep);
      }
      throw new ScriptException(makeError(1, "Script does not include a known script-type in first line"));
    }
  }

}
