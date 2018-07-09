package security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
REF: http://scytl.github.io/restguide/#_security_2
*/
public class User implements Principal {

  private String username; 

  private List<String> roles = new ArrayList<String>();

  public User(String username, String... roles) {
    super();
    this.username = username;
    this.roles = Arrays.asList(roles);
  }
  
  public User(String username, List<String> roles) {
    super();
    this.username = username;
    this.roles = roles;
  }

  @Override
  public String getName() { 
    return username;
  }

  public boolean isUserInRole(String role) { 
    return this.roles.contains(role);
  }
}