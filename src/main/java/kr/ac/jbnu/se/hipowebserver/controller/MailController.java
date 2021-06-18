package kr.ac.jbnu.se.hipowebserver.controller;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class MailController {
    public static MailController mailController = null;

    private static final String FROM = "hipopo0225@gmail.com";
    private static final String FROMNAME = "hipopo0225";

    private static final String SMTP_USERNAME = "hipopo0225@gmail.com";
    private static final String SMTP_PASSWORD = "qlalfqjsgh1!";
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;

    private static final String SUBJECT = " HIPO 아이디/비밀번호 찾기에 관한 이메일";

    public static MailController getInstance() {
        if (mailController == null) {
            mailController = new MailController();
        }

        return mailController;
    }

    private MailController() {

    }

    public void sendIDMail(String userId, String userEmail, String userName) {
        try {
            Properties properties = System.getProperties();
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.smtp.port", PORT);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(properties);

            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(new InternetAddress(FROM, FROMNAME));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
            mimeMessage.setSubject(userName + "님, " + SUBJECT + "입니다.");
            mimeMessage.setText("안녕하세요, HIPO팀 입니다. 요청하신 아이디는 " + userId + " 입니다.");

            Transport transport = session.getTransport();

            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

            transport.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void sendPWMail(String userPW, String userEmail, String userName) {
        try {
            Properties properties = System.getProperties();
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.smtp.port", PORT);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(properties);

            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(new InternetAddress(FROM, FROMNAME));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
            mimeMessage.setSubject(userName + "님, " + SUBJECT + "입니다.");
            mimeMessage.setText("안녕하세요, HIPO팀 입니다. 요청하신 비밀번호는 " + userPW + " 입니다.");

            Transport transport = session.getTransport();

            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

            transport.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}
