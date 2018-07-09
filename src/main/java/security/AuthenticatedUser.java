
package security;

import java.util.List;

/**
 *
 * @author plaul1
 */
public class AuthenticatedUser {

  public AuthenticatedUser(List<String> roles, String firstName, String lastName) {
    this.roles = roles;
    this.firstName = firstName;
    this.lastName = lastName;
  }
  
  private final List<String> roles;
  private final String firstName;
  private final String lastName;

  public List<String> getRoles() {
    return roles;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  } 
}
