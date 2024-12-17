package com.ser;

import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.ITask;
import com.ser.blueline.bpm.IWorkbasketContent;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDoxisServer;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IExternalConfiguration;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.toolbar.Button;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.bpmservice.workbasket.WorkbasketScripting;
import com.ser.evITAWeb.scripting.document.DocumentRibbonButtonAction;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import jakarta.activation.DataContentHandler;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestMailSend extends WorkbasketScripting {
    public ISession ses;
    public TestMailSend(){
        super();
        this.log=super.log;
    }
    //add a button on document View action
    @Override
    public void onInit() throws EvitaWebException {
        log.info("BB WEBCUBE WORKBASKET LOG START...ON INITTTTT");
        try {
            getRibbon().addButton(createExampleButton());
        } catch (EvitaWebException e) {
            throw new RuntimeException(e);
        }
    }
    //creating a button
    public Button createExampleButton() {
        String extensionName = getExtensionReference().extensionName;
        Button btn = Doxis4ClassFactory.getButton();
        btn.setImagePath("extensions/"+extensionName+"/images/smalldoxi.jpg");
        btn.setToolTip("Doxi");
        btn.setTitle("Download File");
        btn.setSynchronizedExecution(true);
        btn.setClassToExecute(TestMailSend.OpenMessageBoxAction.class.getName());
        log.info("Button created");
        return btn;
    }
    //Open a message box when we click a button
    public static class OpenMessageBoxAction extends DocumentRibbonButtonAction {
        @Override
        public IBasicAction onClick(String[] selected) throws EvitaWebException {
            //Logger log = LoggerFactory.getLogger(DownloadFiles.class);

            log.info("start onClick:::exportPath : ");
            try {
                IMessageAction msg = Doxis4ClassFactory.createShowMessageAction();
                IUser usr = getSession().getUser();
                String umail = usr.getEMailAddress();
                List<String> mails = new ArrayList<>();
                if (umail != null) {
                    String to = umail;
                    log.info("SEND MAIL TO.....:" + to);
                    // Sender's email ID needs to be mentioned
                    String from = "doxis@qcon.com.qa";
                    // Assuming you are sending email from localhost
                    String host = "qconho-veeamone.qcon.local";
                    // Get system properties
                    Properties properties = System.getProperties();
                    // Setup mail server
                    properties.setProperty("mail.smtp.host", host);
                    // Get the default Session object.
                    Session session = Session.getDefaultInstance(properties);
// Create a default MimeMessage object.
                    MimeMessage message = new MimeMessage(session);
                    // Set From: header field of the header.
                    message.setFrom(new InternetAddress(from));
                    // Set To: header field of the header.
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    // Set Subject: header field
                    message.setSubject("Download Link for Files!");
                    // Send the actual HTML message, as big as you like
                    message.setContent("<h1>This is actual message</h1>", "text/html");
                    // Send message
                    log.info("SEND MAIL FINAL.....:");
                    Transport.send(message);
                    log.info("Sent message successfully....");
                } else {
                    log.info("Mail adress is null :" + usr.getFullName());
                }
                msg.setMessage("Sent message successfully...." + selected.length);
                return msg;
            } catch (BlueLineException e) {
                log.error("exception", e);
                throw new EvitaWebException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void onFetchWorkbasketContent(IWorkbasketContent iWorkbasketContent) throws EvitaWebException {
        log.info("BB WEBCUBE WORKBASKET LOG START...");
    }
}
