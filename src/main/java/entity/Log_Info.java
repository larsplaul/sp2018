package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;

/**
 *
 * @author plaul1
 */
@Entity
public class Log_Info implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  
  private String userName;
  private String event;
  private String details;
  
  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date logDate;

  public Log_Info(String userName, String event) {
    this.userName = userName;
    this.event = event;
    this.logDate = new Date();
  }
  
  public Log_Info(String userName, String event,String details) {
    this.userName = userName;
    this.event = event;
    this.details = details;
    this.logDate = new Date();
  }

  public Log_Info() {
  }
  
  

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

 
  
}
