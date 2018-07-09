/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(name = "TASK")
public class Task implements Serializable {

  
  public static int CODE_TIMEOUT_MINUTES = 5; //Should be set in web.xml
  
  @ManyToOne
  private SemesterPeriod semesterPeriod;
  private static final long serialVersionUID = 1L;
  @Id
  //@GeneratedValue(strategy = GenerationType.TABLE,generator = "idGenerator")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private int maxScore;

  @Column(length = 6)
  private String code = null;

  public String getCode() {
    return getCode(new Date());
  }
  public String getCode(Date now) {
    hasValidCode(now); //This will set code to null, if timed out
    return code;
  }

  public boolean setCode(String code) {
    if(!this.allowAutoAttendance){
      return false;
    }
    this.code = code;
    this.codeCreated = new Date();
    return true;
  }

  public Date getCodeCreated() {
    return codeCreated;
  }

  public boolean isAllowAutoAttendance() {
    return allowAutoAttendance;
  }

  public void setAllowAutoAttendance(boolean allowAutoAttendance) {
    this.allowAutoAttendance = allowAutoAttendance;
  }
  
  public boolean hasValidCode(Date now){
//    int MIN = 200;
//    long time = -1;
//    boolean allowed = false;
//    if(codeCreated !=null){
//       time = ((codeCreated.getTime()+60*1000 *CODE_TIMEOUT_MINUTES) -  now.getTime());
//       allowed = ((codeCreated.getTime()+60*1000 *CODE_TIMEOUT_MINUTES) >  now.getTime());
//    }
//    System.out.println("Allow: "+allowAutoAttendance+", code: "+(code!= null)+", Time: "+time+", Allowed: "+allowed);
    if(allowAutoAttendance && code!= null && ((codeCreated.getTime()+60*1000 *CODE_TIMEOUT_MINUTES) >  now.getTime())) {
      return true;
    }
    //Assuming that reason was a timeout for the code
    code = null;
    codeCreated = null;
    return false;
  }
  
  boolean allowAutoAttendance = false;
  

  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date codeCreated = null;

  @OneToMany(mappedBy = "task", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.ALL})
  private final List<StudyPoint> studyPoints = new ArrayList();

  public Task() {
  }

//  public Task(int maxScore, String name, SemesterPeriod period) {
//    this.maxScore = maxScore;
//    this.name = name;
//    this.semesterPeriod = period;
//  }
  public Task(int maxScore, String name, SemesterPeriod period, boolean allowAuto) {
    this.maxScore = maxScore;
    this.name = name;
    this.semesterPeriod = period;
    this.allowAutoAttendance = allowAuto;
  }

  public List<StudyPoint> getStudyPoints() {
    return studyPoints;
  }

  public StudyPoint getStudyPointForStudent(int studentId) {
    for (StudyPoint sp : studyPoints) {
      if (sp.getStudyPointUser().getId() == studentId) {
        return sp;
      }
    }
    return null;
  }

  public void addStudyPoint(StudyPoint sp) {
    studyPoints.add(sp);
  }

  public SemesterPeriod getSemesterPeriod() {
    return semesterPeriod;
  }

  public void setSemesterPeriod(SemesterPeriod semesterPeriod) {
    this.semesterPeriod = semesterPeriod;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(int maxScore) {
    this.maxScore = maxScore;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

}
