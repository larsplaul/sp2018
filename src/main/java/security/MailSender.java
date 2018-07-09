package security;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public class MailSender {

  private static String mailServer;
  private static String mailUser;
  private static String mailPassword;

//  public static void initConstants(ServletContext context) {
//    mailServer = context.getInitParameter("mailServer");
//    mailUser = context.getInitParameter("mailUser");
//    mailPassword = context.getInitParameter("mailPassword");
//  }
  public static void initConstants(String server, String user, String pw) {
    mailServer = server;
    mailUser = user;
    mailPassword = pw;
  }

  public static void sendMail(String userEmail, String userName, String tempPassword) {
    
      String to = userEmail;
      String host = "smtp.gmail.com";

      Properties props = new Properties();
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.port", "587");

      // Get the Session object.
      Session session = Session.getInstance(props,
              new javax.mail.Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(mailUser, mailPassword);
        }
      });

      try {

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailUser));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Pasword Reset");
        message.setContent(String.format("<p>Hi <b>%1s</b>. Here is a temporary password you can use to login:  %2s</p> "+
                "<p>Use this password to login, go to 'My Account' and select 'Change Password'. Use this password as your old password, when it's requested </p>"
                + "<p>It's only valid for 15. minuttes, so login, and change your password now</p>", userName, tempPassword), "text/html; charset=utf-8");
        System.out.println("Sending message....");
        Transport.send(message);

        System.out.println("Sent message successfully....");

      } catch (MessagingException e) {
        throw new RuntimeException(e);
      }
    }
  }
