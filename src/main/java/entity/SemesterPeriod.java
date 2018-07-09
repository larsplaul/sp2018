/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;


@Entity
@Table(name="SEMESTER_PERIOD")
public class SemesterPeriod implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  //@GeneratedValue(strategy = GenerationType.TABLE,generator = "idGenerator")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  
  @ManyToOne
  private SP_Class inClass;

  public Integer getId() {
    return id;
  }
  
  String periodName;
  String periodDescription;
  @OneToMany(mappedBy = "semesterPeriod")
  @OrderColumn(name = "insertOrderColumn")
  List<Task> tasks = new ArrayList();

  public SP_Class getInClass() {
    return inClass;
  }

  public void setInClass(SP_Class inClass) {
    this.inClass = inClass;
  }

  public String getPeriodName() {
    return periodName;
  }

  public void setPeriodName(String periodName) {
    this.periodName = periodName;
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String periodDescription) {
    this.periodDescription = periodDescription;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }
  
  public void addTask(Task t){
      this.tasks.add(t);
  }

  
}
