package com.ser;

import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.scripting.record.RecordScripting;

public class OrgUnitDocsRecord extends RecordScripting {
    @Override
    public void onInit() throws EvitaWebException {
        log.info("QCON..WEB CUBE..ORG UNIT RECORD SCRIPTIN STARING...");
        addArchiveScriptingForQuickFiling("*", OrgUnitDocsArchive.class.getName());
        super.onInit();
    }
}
