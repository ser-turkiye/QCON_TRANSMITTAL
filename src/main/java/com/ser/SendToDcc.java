package com.ser;

import com.ser.blueline.*;
import com.ser.blueline.bpm.ITask;
import com.ser.blueline.bpm.ITaskDefinition;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.actions.IStopFurtherAction;
import com.ser.evITAWeb.api.bpmservice.dialogs.ICompleteDialog;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.bpmservice.task.TaskScripting;
import org.slf4j.Logger;
import utils.GeneralLib;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class SendToDcc extends TaskScripting {
    private static Logger log;
    public ISession ses;
    private IDialog dlg;
    public SendToDcc(ITask task){
        super(task);
        this.log = super.log;
    }

    @Override
    public void onInit() throws EvitaWebException {
        boolean isExternal = false;
        ses = getTask().getSession();

        ITaskDefinition taskDefinition = getTask().getTaskDefinition();
        String activityName = taskDefinition.getName();
        if (!isNew()) {
            processView.setDialogType("default");
            return;
        }

        IUnit uiExternal= ses.getDocumentServer().getUnitByName( ses,"ExternalReader");
        IUser usr = getTask().getCreator();
        if(usr == null) usr = ses.getUser();

        List<String> units = Arrays.asList(usr.getUnitIDs());
        isExternal = units.contains(uiExternal.getID());

        processView.setDialogType(isExternal ? "external" : "internal");
    }
    @Override
    public void onInitMetadataDialog(IDialog dialog) throws EvitaWebException {
        this.dlg = dialog;

        if(!isNew()){return;}

        List<IInformationObject> updateList = new ArrayList<>();
        IInformationObject parentObject = getTask().getProcessInstance().getMainInformationObject();

        if(parentObject == null){
            IInformationObjectLinks attachLinks = getTask().getProcessInstance().getLoadedInformationObjectLinks();
            ILink firstAttachLink = (attachLinks.getLinks().size() > 0 ? attachLinks.getLinks().get(0) : null);
            parentObject = (firstAttachLink != null ? firstAttachLink.getTargetInformationObject() : null);
        }
        if(parentObject != null) {
            updateList.add(parentObject);
        }

        IInformationObject prjCardDoc = GeneralLib.getProjectCard(getTask().getSession(),
                parentObject.getDescriptorValue("ccmPRJCard_code"));
        if(prjCardDoc != null){
            updateList.add(prjCardDoc);
        }

        for(IInformationObject sourceObje:updateList){

            Vector<IControl> fields = dlg.getFields();
            for(IControl ctrl : fields){
                // if(!ctrl.isReadonly()) continue;

                if (ctrl.getName() == null || ctrl.getName().isEmpty()) continue;

                String descID = ctrl.getDescriptorId();
                String parentVal = sourceObje.getDescriptorValue(descID);
                if(parentVal == null) continue;
                if(parentVal.isEmpty()) continue;

                Utils.setText(dlg , ctrl.getName() , parentVal);
            }
        }

        super.onInitMetadataDialog(dialog);
    }
    @Override
    public IBasicAction onCommit(IDialog dialog) {
        return validation(dialog, null);
    }
    @Override
    public IBasicAction onComplete(ICompleteDialog taskCompleteDialog, IDialog dialog) {
        return validation(dialog, taskCompleteDialog);
    }

    private IBasicAction validation(IDialog dialog, ICompleteDialog taskCompleteDialog) {
        if(dialog == null){return null;}

        try {
            String prjCode = null;

            IControl _prjCode = dialog.getFieldByName(GeneralLib.Descriptors.ProjectNo);
            if (_prjCode != null && _prjCode instanceof ITextField) {
                prjCode = ((ITextField) _prjCode).getText();
            }
            if (prjCode == null || "".equals(prjCode)) {
                throw new Exception("Invalid Project Code. Kindly re-check your inputs");
            }

            String prjName = null;
            IControl _prjName = dialog.getFieldByName(GeneralLib.Descriptors.ProjectName);
            if (_prjName != null && _prjName instanceof ITextField) {
                prjName = ((ITextField) _prjName).getText();
            }
            if (prjName == null || "".equals(prjName)) {
                throw new Exception("Invalid Project Name. Kindly re-check your inputs");
            }

            if(taskCompleteDialog == null && isNew()){
                IInformationObjectLinks links = getTask().getProcessInstance().getLoadedInformationObjectLinks();

                for (ILink link : links.getLinks()) {
                    IInformationObject xdoc = link.getTargetInformationObject();
                    if (!xdoc.getClassID().equals(GeneralLib.ClassIDs.EngineeringDocument)){continue;}
                    String disp = xdoc.getDisplayName();
                    String dpjn = xdoc.getDescriptorValue(GeneralLib.Descriptors.ProjectNo, String.class);
                    dpjn = (dpjn == null ? "" : dpjn);
                    if(!prjCode.equals(dpjn)){
                        throw new Exception("The process cannot be started for documents in a different project card. \n\n" +
                                "[" + disp + "]");
                    }

                    String dsts = xdoc.getDescriptorValue(GeneralLib.Descriptors.DocStatus, String.class);
                    dsts = (dsts == null ? "" : dsts);
                    if(!GeneralLib.CheckValues.SendDocStatuses.contains(dsts)){
                        throw new Exception("The process cannot be started for this document. \n" +
                                "The process can only be started for documents in 'New' status. \n\n" +
                                "[" + disp + "/" + dsts +"]");
                    }
                }
            }
        }
        catch(Exception ex){
            IStopFurtherAction actn = Doxis4ClassFactory.createStopFurtherAction();
            actn.setMessage(ex.getMessage());
            actn.setType(IMessageAction.EnumMessageType.ERROR);
            return actn;
        }
        return null;
    }
}
