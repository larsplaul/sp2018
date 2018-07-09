
package rest.errorhandling;

import java.io.PrintWriter;
import java.io.StringWriter;


public class ErrorMessage {

  public ErrorMessage(Throwable ex, int code,boolean debug) {
    this.code = code;
    this.message = ex.getMessage();
    //this.description = ex.getMessage();
    if(debug){
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      this.stackTrace = sw.toString();
    }
    ex.printStackTrace();
  }

  public ErrorMessage() {}
  

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }
  
  
  private int code;
  private String message;
  private String description;
  private String stackTrace;
}
