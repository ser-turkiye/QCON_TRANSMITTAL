package com.ser;

import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.actions.IStopFurtherAction;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.IDate;
import com.ser.evITAWeb.api.document.IDocumentView;
import com.ser.evITAWeb.api.toolbar.Button;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.document.DocumentRibbonButtonAction;
import com.ser.evITAWeb.scripting.document.DocumentScripting;
import org.slf4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Document Scripting
public class DateChecker extends DocumentScripting {

    //initialize a logger
    //private static final Logger log = LogManager.getLogger();
    private static Logger log;

    public DateChecker(){
        super();
        this.log=super.log;
    }

    //add a button on document View action
    @Override
    public IBasicAction onInitDocumentView() {

        try {
            getRibbon(IDocumentView.DocumentDisplayItem.CONTENT).addButton(createExampleButton());
        } catch (EvitaWebException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    //creating a button
    private Button createExampleButton() {
        String extensionName = getExtensionReference().extensionName;
        Button btn = Doxis4ClassFactory.getButton();
        btn.setImagePath("extensions/"+extensionName+"/images/smalldoxi.jpg");
        btn.setToolTip("Doxi");
        btn.setTitle("Small Doxi");
        btn.setSynchronizedExecution(true);
        btn.setClassToExecute(OpenMessageBoxAction.class.getName());
        log.info("Button created");
        return btn;
    }

    //Open a message box when we click a button
    public static class OpenMessageBoxAction extends DocumentRibbonButtonAction {
        @Override
        public IBasicAction onClick(String[] selected) {
            IMessageAction msg = Doxis4ClassFactory.createShowMessageAction();
            msg.setCaption("Test Icon");
            msg.setMessage("This is an example of button creation and click!");
            log.info("Message to be thrown");
            return msg;
        }
    }

    //while saving, do a date comparison
    @Override
    public IBasicAction onCommit(IDialog dialog) {
        IControl fieldByName = dialog.getFieldByName("finishDate");
        if (fieldByName instanceof IDate) {
            IDate dateField = (IDate) fieldByName;
            String value = dateField.getText();
            String newYear = "20240101";
            SimpleDateFormat formatter1 = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");
            Date date;
            Date fixedDate;
            try {
                date = formatter1.parse(value);
                fixedDate = formatter2.parse(newYear);
                if (date.before(fixedDate)) {
                    IStopFurtherAction createStopFurtherAction = Doxis4ClassFactory.createStopFurtherAction();
                    createStopFurtherAction.setMessage("Invalid Date - Date should be more recent than this year");
                    createStopFurtherAction.setType(IMessageAction.EnumMessageType.ERROR);
                    log.info("Message to be thrown - error");
                    return createStopFurtherAction;
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    // message when the document is saved
    @Override
    public IBasicAction onCommitted() {
        IMessageAction retAction = Doxis4ClassFactory.createShowMessageAction();
        retAction.setMessage("Document saved");
        return retAction;
    }
}

