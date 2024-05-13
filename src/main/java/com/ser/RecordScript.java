package com.ser;

import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.scripting.archive.ArchiveScripting;
import com.ser.evITAWeb.scripting.record.RecordScripting;
import com.ser.ImportDocFromExcel2;

public class RecordScript extends RecordScripting {
    @Override
    public void onInit() throws EvitaWebException {
        addArchiveScriptingForQuickFiling("*", ImportDocFromExcel2.class.getName());
        super.onInit();
    }
}
