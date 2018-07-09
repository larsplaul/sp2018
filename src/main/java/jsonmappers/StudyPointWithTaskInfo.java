
package jsonmappers;

import entity.StudyPoint;


public class StudyPointWithTaskInfo {

  public StudyPointWithTaskInfo(StudyPoint point) {
    spId = point.getId();
    max = point.getTask().getMaxScore();
    taskName = point.getTask().getName();
  }
  
  public int spId;
  public int max;
  public String taskName;
}
