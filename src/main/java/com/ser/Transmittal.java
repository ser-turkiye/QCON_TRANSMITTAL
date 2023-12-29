package com.ser;
import com.ser.evITAWeb.api.controls.IDate;
import utils.*;

import com.ser.blueline.*;
import com.ser.blueline.bpm.ITask;
import com.ser.blueline.bpm.ITaskDefinition;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.actions.IStopFurtherAction;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.bpmservice.task.TaskScripting;
import org.slf4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Transmittal extends TaskScripting {


    private static Logger log;
    public ISession ses;
    private IDialog dlg;
    public Transmittal(ITask task){
        super(task);
        this.log=super.log;
    }

    @Override
    public void onInit() throws EvitaWebException {

        boolean isExternal = false;
        ses = getTask().getSession();



        ITaskDefinition taskDefinition = getTask().getTaskDefinition();
        String activityName = taskDefinition.getName();
        if (isNew() ) {

            IUnit uiExternal= ses.getDocumentServer().getUnitByName( ses,"ExternalReader");
            IUser usr = getTask().getCreator();
            if(usr==null) usr = ses.getUser();

            List<String> units = Arrays.asList(usr.getUnitIDs());
            isExternal =units.contains(uiExternal.getID()) ;

            processView.setDialogType("internal");
            if (isExternal)  processView.setDialogType("external");

        } else
            processView.setDialogType("default");


       // processView.setItemReadonlyMode(ProcessDisplayItem.DOCUMENT_LINKS, EnumReadonlyMode.READONLY);

    }

    @Override
    public IBasicAction onCommit(IDialog dialog) throws EvitaWebException {
        // Return, if descriptor value not set
        if (dialog != null) {

            IControl fieldByName = dialog.getFieldByName("ccmPRJCard_name");
            if (fieldByName != null && fieldByName instanceof ITextField) {
                ITextField textField = (ITextField) fieldByName;
                String value = textField.getText();
                if (value == null || "".equals(value)) {
                    IStopFurtherAction createStopFurtherAction = Doxis4ClassFactory.createStopFurtherAction();
                    createStopFurtherAction.setMessage("Invalid Project Name. Kindly re-check your inputs");
                    createStopFurtherAction.setType(IMessageAction.EnumMessageType.ERROR);
                    return createStopFurtherAction;
                }
            }
        }
        return null;
    }
@Override
    public void onInitMetadataDialog(IDialog dialog) throws EvitaWebException {
        this.dlg = dialog;
        ses = getTask().getSession();

        String prjCode = "";

        if(!isNew() ) return;
        IUnit uiExternal= ses.getDocumentServer().getUnitByName( ses,"ExternalReader");
        IUser usr = getTask().getCreator();
        if(usr==null) usr = ses.getUser();
        log.info("usr:" + usr.getLogin());

        boolean isExternal = false;
        List<String> units = Arrays.asList(usr.getUnitIDs());
        isExternal = units.contains(uiExternal.getID()) ;

        String mainCompName = GeneralLib.getMainCompGVList(getTask().getSession(),"CCM_PARAM_CONTRACTOR-MEMBERS","NAME");
        String mainCompSName = GeneralLib.getMainCompGVList(getTask().getSession(),"CCM_PARAM_CONTRACTOR-MEMBERS","SNAME");
        log.info("MAIN COM NAME:" + mainCompName);
        log.info("MAIN COM SHORT NAME:" + mainCompSName);

        String from = "";
        String fromSName = "";
        String receiver = "";
        String receiverSName = "";
        String ownerCompSName = "";
        String ownerCompName = "";


        List<IInformationObject> updateList = new ArrayList<>();
        IInformationObject parentObject = getTask().getProcessInstance().getMainInformationObject();

        if(parentObject == null){
            IInformationObjectLinks attachLink= getTask().getProcessInstance().getLoadedInformationObjectLinks();
            parentObject =  attachLink.getLinks().get(0).getTargetInformationObject();
        }

        if(parentObject != null){
            prjCode = parentObject.getDescriptorValue("ccmPRJCard_code");
            updateList.add(parentObject);
            IInformationObject prjCardDoc = GeneralLib.getProjectCard(getTask().getSession(), parentObject.getDescriptorValue("ccmPRJCard_code"));
            log.info("Project card doc : " + prjCardDoc);
            if(prjCardDoc!=null) updateList.add(prjCardDoc);
            for(IInformationObject sourceObje:updateList){

                Vector<IControl> fields = dlg.getFields();
                for(IControl ctrl : fields){
                   // if(!ctrl.isReadonly()) continue;

                    if (ctrl.getName()== null || ctrl.getName().isEmpty()) continue;
                    if (ctrl.getName().equals("ccmPrjDocNumber")) continue;

                    String descID = ctrl.getDescriptorId();
                    String parentVal = sourceObje.getDescriptorValue(descID);
                    if(parentVal == null) continue;
                    if(parentVal.isEmpty()) continue;

                    Utils.setText(dlg , ctrl.getName() , parentVal);
                }
            }
        }

        log.info("external user email:" + usr.getEMailAddress());
        log.info("Project Code:" + prjCode);
        if(isExternal) {
            IDocument ownerContactFile = GeneralLib.getContactRecord(getTask().getSession(), usr.getEMailAddress());
            log.info("owner contact file:" + ownerContactFile);
            if (ownerContactFile != null) {
                log.info("owner contact file code:" + ownerContactFile.getDescriptorValue("ObjectNumber"));
                IDocument contractorFile = GeneralLib.getContractorFolder(prjCode,ownerContactFile.getDescriptorValue("ObjectNumber"));
                log.info("owner contactor file:" + contractorFile);
                ownerCompSName = (contractorFile != null ? contractorFile.getDescriptorValue("ContactShortName") : "");
                ownerCompName = (contractorFile != null ? contractorFile.getDescriptorValue("ObjectName") : "");
            }
            from = ownerCompName;
            fromSName = ownerCompSName;
            receiver = mainCompName;
            receiverSName = mainCompSName;
        }else{
            from = mainCompName;
            fromSName = mainCompSName;
        }
        log.info("from:" + from);
        log.info("receiver:" + receiver);

        IControl _issueDate = dialog.getFieldByName("ccmTrmtSender");
        if (_issueDate != null && _issueDate instanceof IDate) {
            DateFormat dt = new SimpleDateFormat("yyyyMMdd");
            ((ITextField) _issueDate).setText(dt.format(new Date()));
        }

        IControl _sender = dialog.getFieldByName("ccmTrmtSender");
        if (_sender != null && _sender instanceof ITextField) {
            log.info("from :::" + from);
            ((ITextField) _sender).setText(from);
        }
        IControl _senderCode = dialog.getFieldByName("ccmSenderCode");
        if (_senderCode != null && _senderCode instanceof ITextField) {
            log.info("from :::" + from);
            log.info("from shortname :::" + fromSName);
            ((ITextField) _senderCode).setText(fromSName);
        }
        IControl _receiver = dialog.getFieldByName("ccmTrmtReceiver");
        if (_receiver != null && _receiver instanceof ITextField) {
            log.info("receiver :::" + receiver);
            ((ITextField) _receiver).setText(receiver);
        }
        IControl _receiverCosw = dialog.getFieldByName("ccmReceiverCode");
        if (_receiver != null && _receiverCosw instanceof ITextField) {
            log.info("from :::" + from);
            log.info("receiver short name :::" + receiverSName);
            ((ITextField) _receiverCosw).setText(receiverSName);
        }
        super.onInitMetadataDialog(dialog);
    }

    public void onInitMetadataDialogx(final IDialog dialog) throws EvitaWebException {
            /*
        if (dialog != null && isNew()) {

            IDocument projeDoc ;
            IDocument contactDoc = getContactRecord(ses.getUser().getLogin());
            if(contactDoc != null) {

            }

            IControl fieldByName = dialog.getFieldByName("ccmPRJCard_name");
            if (fieldByName != null && fieldByName instanceof ITextField) {
                ITextField textField = (ITextField) fieldByName;
                textField.setText("Yunus Emre Test");
            }
            fieldByName = dialog.getFieldByName("ccmPRJCard_code");
            if (fieldByName != null && fieldByName instanceof ITextField) {
                ITextField textField = (ITextField) fieldByName;
                textField.setText("KN12345");
            }

        }

             */

    }

}
