package com.cakeshopsystem.utils.mail;

import com.cakeshopsystem.utils.dotenv.dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class GmailSMTP {

    private static final String username = dotenv.gmail_account;
    private static final String password = dotenv.app_password;

    private static final String SUBJECT = "Cake Shop System - OTP Code";

    private static Properties getProps() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }

    private static Authenticator getAuthenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    // cache session
    private static final Session SESSION = Session.getInstance(getProps(), getAuthenticator());

    private static Message buildMessage(String to, String body) throws MessagingException {
        Message message = new MimeMessage(SESSION);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(SUBJECT);
        message.setText(body);
        return message;
    }

    public static boolean sendMail(String targetAddress, String body) {
        System.out.println("TO  = " + targetAddress);
        System.out.println("FROM= " + username);
        System.out.println("PASS is null? " + (password == null));
        try {
            Transport.send(buildMessage(targetAddress, body));
            return true;
        } catch (MessagingException err) {
            System.err.println("Error sending email to " + targetAddress + ": " + err.getMessage());
            return false;
        }
    }
}
