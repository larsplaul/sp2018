package security;

import java.math.BigInteger;
import java.security.SecureRandom;


public class TempPasswordGenerator {
private final SecureRandom random = new SecureRandom();

  public String nextPassword() {
    return new BigInteger(130, random).toString(32);
  }  
}
