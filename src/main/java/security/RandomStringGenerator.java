package security;

import java.util.Random;

/**
 *
 * @author plaul1
 */
public class RandomStringGenerator {
   public static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
   public static final int CODE_LENGTH = 6;
   public static String getCode(){
     char[] code = new char[CODE_LENGTH];
     Random rnd = new Random();
     for(int i=0; i < CODE_LENGTH; i++){
       int index = rnd.nextInt(ALLOWED_CHARS.length());
       code[i] = ALLOWED_CHARS.charAt(index);
     }
     return new String(code);
   }
   
   public static void main(String[] args) {
     System.out.println(getCode()); 
  }
}
