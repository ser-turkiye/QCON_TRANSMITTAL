package com.ser;

import com.ser.blueline.IInformationObject;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IStopFurtherAction;
import com.ser.evITAWeb.api.context.IFolderContext;
import com.ser.evITAWeb.api.context.IScriptingContext;
import com.ser.evITAWeb.api.context.ISourceContext;
import com.ser.evITAWeb.api.controls.ICategoryTree;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.archive.ArchiveScripting;
import com.ser.foldermanager.INode;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class OrgUnitDocsArchive extends ArchiveScripting {
    private IDialog dlg;

    private IInformationObject parentFolder;
    public OrgUnitDocsArchive(){
        super();
        this.log=super.log;
    }
    public IInformationObject getSourceInformationObject(ISourceContext sourceContext){
        log.info("QCON WEB CUBE----Getting Source Info Object");
        IScriptingContext source = sourceContext.getSourceContext();
        if(source == null){
            log.info("Didnt come from any source");
            return null;
        }
        if((source instanceof IFolderContext)){
            log.info("Came from a folder");
            IFolderContext folderContext = (IFolderContext) source;
            return folderContext.getFolder();
        }else{
            log.info("Didnt come from a folder");
            return null;
        }
    }
    public INode getSourceNode(ISourceContext sourceContext){
        IFolderContext folderContext = (IFolderContext) sourceContext.getSourceContext();
        if(folderContext!=null) return folderContext.getNode();
        return null;
    }
    @Override
    public void onInit() throws EvitaWebException {
        try {
            log.info("QCON WEB CUBE----Archive OnInit Start.......");
            this.dlg = getDialog();
            parentFolder = getSourceInformationObject(getSourceContext());
            if(parentFolder==null) return;

            INode sourceNode = getSourceNode(getSourceContext());
            ICategoryTree treeDept = dlg.getCategoryTree("ccmPrjDocDepartment");
            String departmentName = parentFolder.getDescriptorValue("ObjectName");
            String deptCode = (departmentName.contains("-") ? departmentName.split("-")[0] : "");
            Vector<IControl> fields = dlg.getFields();
            for(IControl ctrl : fields){
                log.info("CTRL.NAME : " + ctrl.getName());
                String descriptorId = ctrl.getDescriptorId();
                if(descriptorId == null) continue;

                if(descriptorId.equals("f5aeb1b9-9732-4bab-a3e1-34799a7a4fac")) {
                    ((ITextField) ctrl).setText(deptCode);
                }
                if(descriptorId.equals("f07fe204-e87d-4ea5-a935-a184c641d789")) {
                    List<String> dept = new ArrayList<>();
                    dept.add(departmentName);
                    log.info("PARENT DESC VALUE::::" + departmentName);
                    treeDept.setValues(dept.toArray(new String[0]));
                    ctrl.setReadonly(true);
                }

            }
            super.onInit();
        } catch (RuntimeException e) {
            log.error("bbb hata:::", e);
            throw new EvitaWebException(e.getMessage());
        }
    }
    @Override
    public IBasicAction onDescriptorValuesSave() throws EvitaWebException {
        IBasicAction action=null;
        if(parentFolder == null)
            action =createStopFurtherAction("You must create document in related unit record.");

        return action;
    }
    private  ITextField getTextFieldFromDlg(String textFieldName){
        try{
            return dlg.getTextField(textFieldName);
        }catch (Exception e){
            log.error("Exception, couldnt get textfield: " + textFieldName);
            return null;
        }
    }

    private IBasicAction createStopFurtherAction(String message) {
        IStopFurtherAction stopFurtherAction = Doxis4ClassFactory.createStopFurtherAction();
        stopFurtherAction.setMessage(message);
        return stopFurtherAction;
    }


}

