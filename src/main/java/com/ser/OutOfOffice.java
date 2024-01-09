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
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class OutOfOffice extends TaskScripting {


    private static Logger log;
    public ISession ses;
    private IDialog dlg;
    public OutOfOffice(ITask task){
        super(task);
        this.log=super.log;
    }

    public void onInit() throws EvitaWebException {

        boolean isExternal = false;
        ses = getTask().getSession();



        ITaskDefinition taskDefinition = getTask().getTaskDefinition();
        String activityName = taskDefinition.getName();
        if (isNew() ) {

            IUser usr = getTask().getCreator();
            if(usr==null) usr = ses.getUser();

            getTask().setDescriptorValue("AbacOrgaRead",usr.getID());
            getTask().setDescriptorValue("orgUserDelegationFrom",usr.getID());

        }
    }
}
