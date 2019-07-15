/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.printer_status;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.Properties;
import java.util.TimerTask;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Main {

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
                    //line.replaceAll("\\s{2,}", " ").trim();
                    SBappendLine(output, line);
                    SBappendLine(output, "-------------------------------------------------------------------------------------------------------------");
                }

                if (line.contains("RICOH")) {
                    //line.replaceAll("\\s{2,}", " ").trim();
                    if (line.contains("Normal")) {
                        status++;
                    }
                    SBappendLine(output, line);
                    SBappendLine(output, "-------------------------------------------------------------------------------------------------------------");
                }

                if (line.contains("LaserJet")) {
                    //line.replaceAll("\\s{2,}", " ").trim();
                    if (line.contains("Normal")) {
                        status++;
                    }
                    SBappendLine(output, line);
                    SBappendLine(output, "-------------------------------------------------------------------------------------------------------------");
                }
            }

            //System.out.println(output);
            if (status < 3) {
                //System.out.println(status);
                SendAlert(output);
            }

            else {
                //System.out.println(status);
                //System.out.println("Printers are operating Normally");
            }
        }

        catch(Exception e) {

        }
    }

    public static void main(String[] args) throws IOException {

        Timer timer = new Timer();
        long interval = (1*60*1000);
        timer.schedule (new TimerTask() {
            public void run() {
                Run();
            }
        }, 0, interval);

    }
}
