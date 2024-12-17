package com.ser;

import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.scripting.filing.FilingEnvironmentScripting;

public class OrgUnitDocsFiling extends FilingEnvironmentScripting {
    @Override
    public void onInit() throws EvitaWebException {
        log.info("QCON..WEB CUBE..ORG UNIT DOC FILING STARING...");
        this.addArchiveScripting("*", OrgUnitDocsArchive.class.getName());

    }
}
