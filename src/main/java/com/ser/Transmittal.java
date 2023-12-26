package com.ser;
import com.ser.sedna.client.bluelineimpl.bpm.InformationObjectLinks;
import utils.*;

import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.ITask;
import com.ser.blueline.bpm.ITaskDefinition;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.actions.IStopFurtherAction;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.api.options.EnumReadonlyMode;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.bpmservice.task.TaskScripting;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

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
        if (isNew()) {

            IUnit uiExternal= ses.getDocumentServer().getUnitByName( ses,"ExternalReader");
            IUser usr = getTask().getCreator();
            if(usr==null) usr = ses.getUser();

            List<String> units = Arrays.asList(usr.getUnitIDs());
            isExternal =units.contains(uiExternal.getID()) ;

            log.info("Transmittal init oluyor.");
            log.info("External Unit ID:" + uiExternal.getID());
            log.info("User name:" + usr.getName());
            log.info("User Units:" + (usr.getUnitIDs().toString()));


            processView.setDialogType("internal");
            if (isExternal)  processView.setDialogType("external");

        } else
            processView.setDialogType("default");


        log.info("Transmittal init oldu.");
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

        if(!isNew()) return;

        List<IInformationObject> updateList = new ArrayList<>();
        IInformationObject parentObject = getTask().getProcessInstance().getMainInformationObject();

        if(parentObject == null){
            IInformationObjectLinks attachLink= getTask().getProcessInstance().getLoadedInformationObjectLinks();
            parentObject =  attachLink.getLinks().get(0).getTargetInformationObject();
        }

        if( parentObject != null){
            updateList.add(parentObject);
            IInformationObject prjCardDoc = getProjectCard(parentObject.getDescriptorValue("ccmPRJCard_code"));
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
    public IDocument getProjectCard(String pCode)  {
        String objectID ="32e74338-d268-484d-99b0-f90187240549" ;

        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(objectID).append("'")
                .append(" AND ")
                .append("ccmPRJCard_code").append(" = '").append(pCode).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[] {"PRJ_FOLDER"} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public IDocument getContactRecord(String eMail)  {
        String SupplierContactWS ="d7ffea9d-3419-4922-8ffa-a0310add5723" ;

        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(SupplierContactWS).append("'")
                .append(" AND ")
                .append("PrimaryEMail").append(" = '").append(eMail).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[] {"BPWS"} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public IInformationObject[] createQuery(String[] dbNames , String whereClause , int maxHits){
        String[] databaseNames = dbNames;

        ISerClassFactory fac = ses.getDocumentServer().getClassFactory();
        IQueryParameter que = fac.getQueryParameterInstance(
                ses ,
                databaseNames ,
                fac.getExpressionInstance(whereClause) ,
                null,null);
        if(maxHits > 0) {
            que.setMaxHits(maxHits);
            que.setHitLimit(maxHits + 1);
            que.setHitLimitThreshold(maxHits + 1);
        }
        IDocumentHitList hits = que.getSession() != null? que.getSession().getDocumentServer().query(que, que.getSession()):null;
        if(hits == null) return null;
        else return hits.getInformationObjects();
    }

}
