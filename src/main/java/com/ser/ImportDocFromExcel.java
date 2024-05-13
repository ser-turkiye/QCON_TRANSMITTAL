package com.ser;

import com.ser.blueline.IDocument;
import com.ser.blueline.metaDataComponents.IArchiveClass;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDialog;
import com.ser.blueline.*;
import com.ser.evITAWeb.api.controls.IControl;
import com.ser.evITAWeb.api.controls.ITextField;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IOpenDialogAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.document.IDocumentView;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.document.*;
import com.ser.evITAWeb.scripting.filing.FilingEnvironmentScripting;
import com.ser.evITAWeb.scripting.archive.ArchiveScripting;
import org.slf4j.Logger;

public class ImportDocFromExcel extends FilingEnvironmentScripting {
    @Override
    public void onInit() throws EvitaWebException {
        this.addArchiveScripting("*", ImportDocFromExcel2.class.getName());

    }
}

