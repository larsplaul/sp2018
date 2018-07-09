package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "SP_CLASS")
public class SP_Class implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  private String id;
  
  private String nameShort;
  private String semesterDescription; //year (fall/spring)
  private int requiredPoints;
  private int maxPointForSemester;

  public int getRequiredPoints() {
    return requiredPoints;
  }

  public void setRequiredPoints(int requiredPoints) {
    this.requiredPoints = requiredPoints;
  }

  public int getMaxPointForSemester() {
    return maxPointForSemester;
  }

  public void setMaxPointForSemester(int maxPointForSemester) {
    this.maxPointForSemester = maxPointForSemester;
  }
  
  @OneToMany(mappedBy = "inClass")
  private  List<SemesterPeriod> periods = new ArrayList();

  public void setPeriods(List<SemesterPeriod> periods) {
    this.periods = periods;
  }

  public void setUsers(List<StudyPointUser> users) {
    this.users = users;
  }
  
  
  @ManyToMany
  @OrderBy("id")
  private List<StudyPointUser> users = new ArrayList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public void addStudyPointUser(StudyPointUser spUser){
    users.add(spUser);
  }

  public void addPeriod(SemesterPeriod period){
      periods.add(period);
  }
  
  public List<SemesterPeriod> getPeriods() {
    return periods;
  }

  public List<StudyPointUser> getUsers() {
    return users;
  }

  public String getNameShort() {
    return nameShort;
  }

  public void setNameShort(String nameShort) {
    this.nameShort = nameShort;
  }

  public String getSemesterDescription() {
    return semesterDescription;
  }

  public void setSemesterDescription(String semesterDescriptor) {
    this.semesterDescription = semesterDescriptor;
  }

  public int getRequiredPointsForSemester() {
    return requiredPoints;
  }

  public void setRequiredPointsForSemester(int requiredPointsForSemester) {
    this.requiredPoints = requiredPointsForSemester;
  }
  
}
