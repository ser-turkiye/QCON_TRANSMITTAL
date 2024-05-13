package com.ser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.ser.blueline.IInformationObject;
import com.ser.blueline.metaDataComponents.IDocumentTemplate;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.actions.IMessageAction.EnumMessageType;
import com.ser.evITAWeb.api.archive.IArchiveDlg;
import com.ser.evITAWeb.api.context.IDocumentContext;
import com.ser.evITAWeb.api.context.IFolderContext;
import com.ser.evITAWeb.api.context.IScriptingContext;
import com.ser.evITAWeb.api.context.ISourceContext;
import com.ser.evITAWeb.api.controls.IButton;
import com.ser.evITAWeb.api.controls.ICategoryTree;
import com.ser.evITAWeb.api.controls.ICheckbox;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.IDBRecordSelector;
import com.ser.evITAWeb.api.controls.IDate;
import com.ser.evITAWeb.api.controls.ILabel;
import com.ser.evITAWeb.api.controls.IMultiValueSelectionBox;
import com.ser.evITAWeb.api.controls.ISelectionBox;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.api.navigation.IFilingElements;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.archive.ArchiveScripting;
import com.ser.evITAWeb.scripting.control.ButtonScripting;
import com.ser.evITAWeb.scripting.control.CategoryTreeControlScripting;
import com.ser.evITAWeb.scripting.control.CheckBoxScripting;
import com.ser.evITAWeb.scripting.control.DBRecordSelectorControlScripting;
import com.ser.evITAWeb.scripting.control.DateScripting;
import com.ser.evITAWeb.scripting.control.MultiValueSelectionBoxScripting;
import com.ser.evITAWeb.scripting.control.SelectionBoxScripting;
import com.ser.evITAWeb.scripting.control.TextFieldScripting;
import com.ser.foldermanager.INode;

/**
 * Example class that demonstrates control scripting for selection boxes, buttons, text boxes and date fields. Requires an archive dialog with the following controls:<BR>
 * <UL>
 * <LI>selection boxes named: src1, dest1, dest2</LI>
 * <LI>a button named: src2</LI>
 * <LI>a category tree control named: cattree1</LI>
 * <LI>a DB Record selector named: dbrs1</LI>
 * <LI>a checkbox named: disableall</LI>
 * <LI>text boxes named: Desk1, Desk2, Desk3</LI>
 * <LI>a button named: save</LI>
 * <LI>a date field named: demoDate</LI>
 * </UL>
 * The selection box "src1" is initialized with the values "action_item_1", "action_item_2", "else_action_item". The remaining text boxes and selection boxes are empty on init. <BR>
 * <A href=FilingDocumentMetadataControlScriptingSample.java.html target=_blank>Display Java source code in separate window.</A><BR>
 * <B>Note</B> Javascript should no longer be used to program controls. However, existing scripts will still be executed as long as there is no Java code for the same control. If both Javascript and
 * Java code exists, the Java code will be executed.
 **/
public class ImportDocFromExcel2 extends ArchiveScripting {
    private static final String DESK1 = "ccmPRJCard_code";
    private IDialog dlg;
    public IInformationObject getSourceInformationObject(ISourceContext sourceContext){
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
        return folderContext.getNode();
    }
    @Override
    public void onInit() throws EvitaWebException {
        try {
            this.dlg = getDialog();
            IInformationObject parentFolder = getSourceInformationObject(getSourceContext());
            if(parentFolder == null) return;
            INode sourceNode = getSourceNode(getSourceContext());
            Vector<IControl> fields = dlg.getFields();
            for(IControl ctrl : fields){
                String descriptorId = ctrl.getDescriptorId();
                if(descriptorId == null) continue;
                String descriptorValue = parentFolder.getDescriptorValue(descriptorId);
                log.info("DESC.ID:" + descriptorId);
                log.info("DESC.VALUE:" + descriptorValue);
                if(!(ctrl instanceof ITextField)) continue;
                if(descriptorValue != null){
                    ((ITextField) ctrl).setText(descriptorValue);
                }
            }
            super.onInit();
        } catch (RuntimeException e) {
            log.error("bbb hata:::", e);
            throw new EvitaWebException(e.getMessage());
        }
    }

    private  ITextField getTextFieldFromDlg(String textFieldName){
        try{
            return dlg.getTextField(textFieldName);
        }catch (Exception e){
            log.error("Exception, couldnt get textfield: " + textFieldName);
            return null;
        }
    }

}

