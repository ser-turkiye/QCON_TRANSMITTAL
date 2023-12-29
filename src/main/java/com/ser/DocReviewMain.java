package com.ser;

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
import utils.GeneralLib;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class DocReviewMain extends TaskScripting {


    private static Logger log;
    public ISession ses;
    private IDialog dlg;
    public DocReviewMain(ITask task){
        super(task);
        this.log=super.log;
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

        if(!isNew() ) return;

        List<IInformationObject> updateList = new ArrayList<>();
        IInformationObject parentObject = getTask().getProcessInstance().getMainInformationObject();

        if(parentObject == null){
            IInformationObjectLinks attachLink= getTask().getProcessInstance().getLoadedInformationObjectLinks();
            parentObject =  attachLink.getLinks().get(0).getTargetInformationObject();
        }

        if( parentObject != null){
            updateList.add(parentObject);
            IInformationObject prjCardDoc = GeneralLib.getProjectCard(getTask().getSession(), parentObject.getDescriptorValue("ccmPRJCard_code"));
            if(prjCardDoc!=null) updateList.add(prjCardDoc);

                for(IInformationObject sourceObje:updateList){

                    Vector<IControl> fields = dlg.getFields();
                    for(IControl ctrl : fields){
                       // if(!ctrl.isReadonly()) continue;

                        if (ctrl.getName()== null || ctrl.getName().isEmpty()) continue;

                        String descID = ctrl.getDescriptorId();
                        String parentVal = sourceObje.getDescriptorValue(descID);
                        if(parentVal == null) continue;
                        if(parentVal.isEmpty()) continue;

                        Utils.setText(dlg , ctrl.getName() , parentVal);


                    }

                }

        }

        super.onInitMetadataDialog(dialog);
    }

}
