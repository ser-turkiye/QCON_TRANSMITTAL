package com.ser;

import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
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

public class DocReviewMain extends TaskScripting {


    private static Logger log;
    public ISession ses;
    private IDialog dlg;
    public DocReviewMain(ITask task){
        super(task);
        this.log=super.log;
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

            if(taskCompleteDialog == null){
                IInformationObjectLinks xlnks = getTask().getProcessInstance().getLoadedInformationObjectLinks();
                List<ILink> links = xlnks.getLinks();
                List<IInformationObject> rmvs = new ArrayList<>();

                for (ILink link : links) {
                    IInformationObject xdoc = link.getTargetInformationObject();
                    if(!xdoc.getClassID().equals(GeneralLib.ClassIDs.EngineeringDocument)){continue;}

                    String disp = xdoc.getDisplayName();
                    String dpjn = xdoc.getDescriptorValue(GeneralLib.Descriptors.ProjectNo, String.class);
                    dpjn = (dpjn == null ? "" : dpjn);
                    if(!prjCode.equals(dpjn)){
                        throw new Exception("The process cannot be started for documents in a different project card. \n\n" +
                                "[" + disp + "]");
                    }

                    //ILink[] ulnks = getDocServer().getReferencedRelationships(getSession(), ((IInformationObject) xdoc), true,true,true);
                    ILink[] ulnks = getDocServer().getReferencingRelationships(getSession(), xdoc.getID(), true,true, getSession().getDatabaseByName("BPM"));


                    for(ILink ulnk : ulnks){
                        IInformationObject lsdoc = ulnk.getSourceInformationObject();
                        String sClsId = lsdoc.getClassID();
                        if(!sClsId.equals(GeneralLib.ClassIDs.DocMainReview)){continue;}

                        ITask stsk = getBpmService().findTask(lsdoc.getID());
                        if(stsk == null){continue;}
                        if(stsk.getFinishedDate() != null){continue;}

                        //IProcessInstance spin = stsk.getProcessInstance();
                        //if(spin.getFinishedDate() != null){continue;}

                        rmvs.add(xdoc);
                        break;

                        //slnks.add("LDOC-SOURCE-" + cnt + " [" + sClsId + "] ::: [" + sId + "]");
                        //slnks.add("LDOC-TARGET-" + cnt + " [" + tClsId + "] ::: [" + tId + "]");
                    }

                    //throw new Exception("SLNKS[" + disp + "]---> \n\n\n[" + String.join("\n\n", slnks) + "]");
                    //throw new Exception("XLNKS.size ::: [" + xlnks.length + "]");
                }

                for(IInformationObject xrmv : rmvs){
                    xlnks.removeInformationObject(xrmv.getID(), true);
                }

                xlnks = getTask().getProcessInstance().getLoadedInformationObjectLinks();
                links = xlnks.getLinks();

                if(links.size() == 0){
                    throw new Exception("Please select document");
                }
            }

            //throw new Exception("test ... ");
        }
        catch(Exception ex){
            IStopFurtherAction actn = Doxis4ClassFactory.createStopFurtherAction();
            actn.setMessage(ex.getMessage());
            actn.setType(IMessageAction.EnumMessageType.ERROR);
            return actn;
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
