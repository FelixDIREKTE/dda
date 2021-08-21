package com.dd.dda.service;

import com.dd.dda.config.NoreplyMailConfiguration;
import com.dd.dda.config.ThreadConfig;
import com.dd.dda.model.sqldata.Comment;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.thread.MailSendingThread;
import com.sun.mail.smtp.SMTPTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;


@Slf4j
@Service
public class MailService {

    private final NoreplyMailConfiguration noreplyMailConfiguration;


    /*Benachrichtigung bei Registrierung
    Passwort zurücksetzen
    Benachrichtigung bei Duplikaten*/



    public MailService(NoreplyMailConfiguration noreplyMailConfiguration) {
        this.noreplyMailConfiguration = noreplyMailConfiguration;
    }
    /*
    private void ssmtp() throws IOException, InterruptedException {
        Process p = new ProcessBuilder("ssmtp").start();
        PrintStream out = new PrintStream(p.getOutputStream());
        out.println("testmessage");
        out.close();


        ProcessBuilder builder = new ProcessBuilder();
        builder.command("ssmtp", "email@gmail.com", "<", "msg.txt");
        Process p = builder.start();

        //echo "inhalt" | mail -s "betreff" felix@montenegros.de

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("echo", "\"inhalt\"", "|", "mail", "-s", "\"betreff\"", "felix@montenegros.de");
        Process p = builder.start();



        String cmd = "eche \"inhalt\"";
        cmd = "echo \"Mail-Inhalt\" | ssmtp -4 felix@montenegros.de";


        cmd = "echo \"inhalt\" | mail -s \"betreff\" felix@montenegros.de";
        Process pythonProcess = Runtime.getRuntime().exec(cmd);
        pythonProcess.waitFor();
        System.out.println(pythonProcess.exitValue());

    }*/

    public void sendRegistrationConfirmation(String email, String emailverif){
        // for example, smtp.mailgun.org
        String EMAIL_SUBJECT = "Registrierung bei Demokratie DIREKT!";
        //String EMAIL_TEXT = "Hallo! \nDein Account wurde bei abstimmung.demokratiedirekt.info registriert. \nFalls Du die Registrierung vorgenommen hast, ist nichts weiter zu tun. Andernfalls solltest Du in Betracht ziehen, uns über diedirekte@posteo.de zu informieren. \n Viel Freude beim Diskutieren und Abstimmen wünscht dir das Team der DIREKTEN!";
        String EMAIL_TEXT = "Hallo! \nDein Account wurde bei abstimmung.demokratiedirekt.info registriert. \nFalls Du die Registrierung vorgenommen hast, klicke https://abstimmung.demokratiedirekt.info/parlamentauswahl.html?ve="+emailverif+" um deine Emailadresse zu verifizieren. Falls Du keine Registrierung vorgenommen hast, solltest Du in Betracht ziehen, uns über diedirekte@posteo.de zu informieren. \n Viel Freude beim Diskutieren und Abstimmen wünscht dir das Team der DIREKTEN!";
        sendMailStmps(email, "", EMAIL_SUBJECT, EMAIL_TEXT);
    }

    public void sendPasswordReset(String email, String password){
        // for example, smtp.mailgun.org
        String EMAIL_SUBJECT = "Passwort zurückgesetz bei Demokratie DIREKT!";
        String EMAIL_TEXT = "Hallo! \nÜber deine Email-Adresse wurde ein neues Passwort angefragt. \nDein neues Passwort lautet "+password+". \n Viele Grüße \ndas Team der DIREKTEN";
        sendMailStmps(email, "", EMAIL_SUBJECT, EMAIL_TEXT);
    }

    public void sendDuplicateInfo(User u1, User u2){
        // for example, smtp.mailgun.org
        String EMAIL_SUBJECT = "Potentielle doppelte Registrierung: " + u1.getId() + " ~ " + u2.getId();
        String EMAIL_TEXT = u1.toString() + "\n\n\n\n" + u2.toString();
        sendMailStmps(noreplyMailConfiguration.getUsername(), noreplyMailConfiguration.getDuplicateInfoDestinations(), EMAIL_SUBJECT, EMAIL_TEXT);
    }


    public void sendMailParallel(String EMAIL_TO, String EMAIL_TO_CC, String EMAIL_SUBJECT, String EMAIL_TEXT){

        ApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadConfig.class);
        MailSendingThread mailSendingThread = (MailSendingThread) ctx.getBean("mailSendingThread");

        mailSendingThread.init(EMAIL_TO, EMAIL_TO_CC, EMAIL_SUBJECT, EMAIL_TEXT, this);
        mailSendingThread.start();
    }


    public void sendMailStmps_(String EMAIL_TO, String EMAIL_TO_CC, String EMAIL_SUBJECT, String EMAIL_TEXT) {
        Properties props = new Properties();

        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", noreplyMailConfiguration.getSmtpServer());
        props.put("mail.smtps.auth", "true");
        // props.put("mail.smtps.quitwait", "false");
        props.put("mail.smtp.ssl.enable", "true");

        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(true);
        try {
            Transport transport = mailSession.getTransport();
            MimeMessage message = new MimeMessage(mailSession);

            message.setSubject(EMAIL_SUBJECT);
            message.setContent(EMAIL_TEXT, "text/plain");

            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(EMAIL_TO));

            transport.connect
                    (noreplyMailConfiguration.getSmtpServer(),
                            465,
                            noreplyMailConfiguration.getUsername(),
                            noreplyMailConfiguration.getPassword());

            transport.sendMessage(message,
                    message.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (MessagingException e) {
            log.error("Mail konnte nicht geschickt werden");
            String errorMessage = "\n" + e.toString();
            errorMessage = errorMessage +  "\n" +e.getMessage();

            for (Object o : e.getStackTrace()){
                errorMessage = errorMessage + "\n" + o;
            }
            log.error(errorMessage);
        }
    }

    public void sendMailStmps___(String to, String cc, String subject, String body)   {
        final Properties p = new Properties();
        p.put("mail.smtp.host", "localhost");
        final Message msg = new MimeMessage(Session.getDefaultInstance(p));
        try {
            msg.setFrom(new InternetAddress(noreplyMailConfiguration.getUsername()));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(body);
            Transport.send(msg);
        } catch(MessagingException e){
            log.error("Mail konnte nicht geschickt werden");
            String errorMessage = "\n" + e.toString();
            errorMessage = errorMessage +  "\n" +e.getMessage();

            for (Object o : e.getStackTrace()){
                errorMessage = errorMessage + "\n" + o;
            }
            log.error(errorMessage);
        }
    }


    public void sendMailStmps(String EMAIL_TO, String EMAIL_TO_CC, String EMAIL_SUBJECT, String EMAIL_TEXT) {

        String SMTP_SERVER = noreplyMailConfiguration.getSmtpServer();
        String USERNAME = noreplyMailConfiguration.getUsername();
        String PASSWORD = noreplyMailConfiguration.getPassword();
        String EMAIL_FROM = noreplyMailConfiguration.getEmailFrom();

        Properties prop = System.getProperties();



        prop.put("mail.smtp.host", SMTP_SERVER); //optional, defined in SMTPTransport
        //prop.put("mail.smtp.ssl.trust", SMTP_SERVER);
        //prop.put("mail.smtp.ssl.trust", "*");

        prop.put("mail.smtp.auth", "true");

        if(false) {

            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.port", "25");

        } else {
            prop.put("mail.smtp.starttls.enable", "false");
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.port", "465");
        }

        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(prop, null);
        Message msg = new MimeMessage(session);



        try {
            msg.setFrom(new InternetAddress(EMAIL_FROM));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(EMAIL_TO, false));
            msg.setRecipients(Message.RecipientType.CC,
                    InternetAddress.parse(EMAIL_TO_CC, false));
            msg.setSubject(EMAIL_SUBJECT);
            msg.setText(EMAIL_TEXT);
            msg.setSentDate(new Date());
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
            t.connect(SMTP_SERVER, USERNAME, PASSWORD); //HOSTNAME???
            t.sendMessage(msg, msg.getAllRecipients());
            log.info("Response: " + t.getLastServerResponse());
            t.close();
            log.info("Mail geschickt");
        } catch (MessagingException e) {
            log.error("Mail konnte nicht geschickt werden");
            String errorMessage = "\n" + e.toString();
            errorMessage = errorMessage +  "\n" +e.getMessage();

            for (Object o : e.getStackTrace()){
                errorMessage = errorMessage + "\n" + o;
            }
            log.error(errorMessage);
        }
    }

    public void sendVerificationPositiveMessage(User verifiedUser) {
        // for example, smtp.mailgun.org
        String EMAIL_SUBJECT = "Dein Account wurde verifiziert!";
        String EMAIL_TEXT = "Hallo "+verifiedUser.getFirstname() +"! \nDein Account wurde verifiziert. Du kannst jetzt über Gesetze abstimmen, Kommentare schreiben, und Beiträge in Diskussionen und Initiativen erstellen. \nViele Grüße \ndas Team der DIREKTEN";
        sendMailParallel(verifiedUser.getEmail(), "", EMAIL_SUBJECT, EMAIL_TEXT);
    }

    public void sendVerificationNegativeMessage(User verifiedUser, String msg) {
        // for example, smtp.mailgun.org
        String EMAIL_SUBJECT = "Problem bei der Verifizierung deines Accounts";
        sendMailStmps(verifiedUser.getEmail(), "", EMAIL_SUBJECT, msg);

    }

    public void sendDuplicateWarning(User fraud) {
        String EMAIL_SUBJECT = "Doppelte Registrierung bei Demokratie DIREKT";
        String EMAIL_TEXT = "Hallo " + fraud.getFirstname() + "! \nAnscheinend ist ein Nutzer mit Deinen persönlichen Daten bereits bei uns registriert. Wir mussten daher deinen Account unter dieser Emailadresse vorerst deaktivieren. \nFalls Du keinen anderen Account angelegt hast, informiere uns bitte über diedirekte@posteo.de und wir klären diesen Fall! \nViele Grüße \ndas Team der DIREKTEN" ;
        sendMailStmps(fraud.getEmail(), "", EMAIL_SUBJECT, EMAIL_TEXT);

    }


    public void reportComment(User reporter, Comment reportedComment) {

        // for example, smtp.mailgun.org
        String EMAIL_SUBJECT = "Kommentar gemeldet: " + reportedComment.getId() + " von " + reportedComment.getUser().getId() + ", gemeldet von " + reporter.getId();
        String EMAIL_TEXT = "Reporter: \n" + reporter.toString() + "\n\n\n\n" +
                        "Reported User: " + reportedComment.getUser() + "\n\n\n\n" +
                         "Reported Comment: " + reportedComment.toString();
        sendMailStmps(noreplyMailConfiguration.getUsername(), noreplyMailConfiguration.getDuplicateInfoDestinations(), EMAIL_SUBJECT, EMAIL_TEXT);
    }

    public void sendReminderMails(User u) {
        String name = " " + u.getName();
        if(u.getName() == null ){
            name = "";
        }
        String EMAIL_SUBJECT = "Erinnerung: Verifiziere deinen Account um abzustimmen!";
        String EMAIL_TEXT = "Hallo" + name + "! \n" +
                "Wir freuen uns, dich in der Community der DIREKTEn dabei zu haben! Damit wir deine Stimme zählen können, müssen wir aber noch verifizieren, dass du ein echter, wahlberechtigter Mensch bist. Bitte vervollständige dazu die Angaben unter \"Benutzerkonto\". Falls du Fragen hast, kannst Du uns gerne unter diedirekte@posteo.de kontaktieren."
                +" \nViele Grüße \ndas Team der DIREKTEN" ;
        sendMailStmps(u.getEmail(), "", EMAIL_SUBJECT, EMAIL_TEXT);
    }
}
