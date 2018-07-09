package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.Size;
import security.PasswordStorage;
//import org.hibernate.validator.constraints.Email;

/**
 *
 * @author plaul1
 */
@Entity
@Table(name = "STUDYPOINT_USER")
@NamedQueries({
    @NamedQuery(name = "StudyPointUser.findByUsername", query = "SELECT s FROM StudyPointUser s WHERE s.userName = :username"),
    @NamedQuery(name = "StudyPointUser.findByEmail", query = "SELECT s FROM StudyPointUser s WHERE s.email = :email")
})
public class StudyPointUser implements Serializable {

    public static int tempPasswordTimeoutMinutes=0; //Must be set in web.xml
  
    private static final long serialVersionUID = 1L;
    @Id
    //@TableGenerator(table = "IDs", name = "idGenerator", initialValue = 10000)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToMany(mappedBy = "users")
    private List<SP_Class> classes;

    public List<SP_Class> get_Classes() {
        return classes;
    }

    public StudyPointUser(String userName, String firstName, String lastName, String email, String phone) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordInitial = userName;
        this.email = email;
        this.phone = phone;
        this.password = userName;
    }

    public StudyPointUser() {
    }

    @OneToMany(mappedBy = "studyPointUser", cascade = CascadeType.ALL)
    //@OrderBy("id")
    @OrderColumn(name = "insertOrderColumn")
    private List<StudyPoint> studyPoints = new ArrayList();

    public void setStudyPoints(List<StudyPoint> studyPoints) {
        this.studyPoints = studyPoints;
    }

    public List<StudyPoint> getStudyPoints() {
        return studyPoints;
    }

    public List<StudyPoint> getStudyPoints(int period_id) {
        List<StudyPoint> filteredPoints = new ArrayList();
        for (StudyPoint p : studyPoints) {
            if (p.getTask().getSemesterPeriod().getId() == period_id) {
                filteredPoints.add(p);
            }
        }
        return filteredPoints;
    }

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "STUDYPOINTUSER_ROLES", joinColumns = {
        @JoinColumn(name = "userName", referencedColumnName = "userName")}, inverseJoinColumns = {
        @JoinColumn(name = "roleName")})

    private List<UserRole> roles = new ArrayList();

    @Column(unique = true)
    @Size(min = 2, message = "Username must include a minimum of two characters")
    private String userName;

    private String firstName;
    private String lastName;
    private String passwordInitial;

  public List<SP_Class> getClasses() {
    return classes;
  }

  public void setClasses(List<SP_Class> classes) {
    this.classes = classes;
  }

  public Date getTempPasswordCreated() {
    return tempPasswordCreated;
  }

  public void setTempPasswordCreated(Date tempPasswordCreated) {
    this.tempPasswordCreated = tempPasswordCreated;
  }
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date tempPasswordCreated = null;

    // @Email   //Hibernate validation, not JPA
    private String email;
    private String phone;
    //private String UserRole;
    private String password;

    public void addRole(UserRole role) {
        roles.add(role);
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public List<String> getRolesAsStrings() {
        if (roles.isEmpty()) {
            return null;
        }
        List<String> rolesAsStrings = new ArrayList();
        for (UserRole role : roles) {
            rolesAsStrings.add(role.getRoleName());
        }
        return rolesAsStrings;
    }

    public Integer getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPasswordInitial() {
        return passwordInitial;
    }

    public void setPasswordInitial(String passwordInitial) {
        this.passwordInitial = passwordInitial;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

//  public String getRole() {
//    return UserRole;
//  }
//
//  public void setRole(String Role) {
//    this.UserRole = Role;
//  }
    public String getPassword() throws AuthenticationException {
      if(tempPasswordCreated != null && (tempPasswordCreated.getTime() +(60*1000*tempPasswordTimeoutMinutes) < new Date().getTime())){
        throw new AuthenticationException("Temporaly password has timed out");     
      }
      return password;
    }

    public void setPassword(String password) throws PasswordStorage.CannotPerformOperationException {
        //this.password = password;
        this.tempPasswordCreated = null;
        this.password = security.PasswordStorage.createHash(password);
    }
    public void setTempPassword(String password) throws PasswordStorage.CannotPerformOperationException {
        this.tempPasswordCreated = new Date();
        this.password = security.PasswordStorage.createHash(password);
    }

    public void addClass(SP_Class _class) {
        if (classes == null) {
            classes = new ArrayList();
        }
        classes.add(_class);
    }

    public void addStudyPoint(StudyPoint sp) {
        studyPoints.add(sp);
    }

}
