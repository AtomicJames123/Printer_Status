package com.mycompany.printer_status;

import static com.mycompany.printer_status.Main.Run;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FXMLController implements Initializable {   
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        Timer timer = new Timer();
        long interval = (1*60*1000);
        timer.schedule (new TimerTask() {
            public void run() {
                Run();
            }
        }, 0, interval);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public static void SBappendLine(StringBuilder builder, String output) {
        builder.append(output);
        builder.append(System.getProperty("line.separator"));
    }

    public static void SendAlert(StringBuilder output) {
        String to = "Email Address of Recipient";

        String from = "Email Address of Sender";
        final String username = "Email Username";
        final String password = "Email Password";

        String host = "smtp.gmail.com";
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "587");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }); // Gmail authentication
        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            message.setSubject("22 Courtlandt Printer Status");
            message.setText(output.toString());

            Transport.send(message);

            System.out.println("Message was sent successfully");
        }

        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void Run() {
        try {
            String command = "powershell.exe Get-Printer | SELECT Name, PrinterStatus";
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            powerShellProcess.getOutputStream().close();
            int status = 0;
            String line;
            StringBuilder output = new StringBuilder();
            BufferedReader out = new BufferedReader(new InputStreamReader(
                    powerShellProcess.getInputStream()));
            while ((line = out.readLine()) != null) {

                if (line.contains("Name")) {
                    SBappendLine(output, line);
                    SBappendLine(output, "-------------------------------------------------------------------------------------------------------------");
                }

                if (line.contains("LaserJet")) {
                    if (line.contains("Normal")) {
                        status++;
                    }
                    SBappendLine(output, line);
                    SBappendLine(output, "-------------------------------------------------------------------------------------------------------------");
                }
            }

            if (status < 3) {
                SendAlert(output);
            }

            else {

            }
        }

        catch(Exception e) {

        }
    }
}
