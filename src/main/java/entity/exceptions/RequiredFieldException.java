package entity.exceptions;

/**
 *
 * @author plaul1
 */
public class RequiredFieldException extends Exception{

  public RequiredFieldException(String string) {
    super(string);
  }

  public RequiredFieldException(String string, Throwable thrwbl) {
    super(string, thrwbl);
  }
  
}
