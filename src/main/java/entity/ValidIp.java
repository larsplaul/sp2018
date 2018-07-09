package entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 *
 * @author plaul1
 * List of IP's (on the School) which makes it possible to self-register
 */
@Entity
public class ValidIp implements Serializable {

  
  private static final long serialVersionUID = 1L;
  @Id
  private String ip;

  public ValidIp(String ip) {
    this.ip = ip;
  }

  public ValidIp() {}
   

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }
 
}
