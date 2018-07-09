package security;

import java.util.Map;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FronterLogin {

  public static boolean authenticateViaFronter(String username, String password) {
    try {
    Connection.Response loginPage = Jsoup.connect("https://fronter.com/cphbusiness/")
            .method(Connection.Method.GET).execute();
    Map cookies = loginPage.cookies();   
    Document doc = loginPage.parse();
    String requestToken = doc.select("[name=fronter_request_token]").first().val();
    String sso = doc.select("[name=SSO_COMMAND_SECHASH]").first().val();
    Connection.Response loginResponse = Jsoup.connect("https://fronter.com/cphbusiness/index.phtml")
            .data("username", username)
            .data("password", password)
            .data("fronter_request_token", requestToken)
            .data("SSO_COMMAND_SECHASH", sso)
            .data("saveid","-1")
            .data("newlang", "en")
            .data("mainurl", "main.phtml")
            .data("screenSize", "1920x1080")
            .header("Content-Type","application/x-www-form-urlencoded")
	        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
            .postDataCharset("UTF-8")
			.header("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36")
			.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute();
    
    String session_userkey = loginResponse.cookie("session_userkey");
    String wcid = loginResponse.cookie("wcid");
    return session_userkey != null && wcid != null;
    
    } catch(Exception e){ 
        System.out.println("EX: "+e.getMessage());
      //Just return false //Not Authenticated, for whatever reason
     }
    return false;
  }
  
  public static void main(String[] args) {
    System.out.println(authenticateViaFronter("lam", "xxxxxxxx"));
  }

}
