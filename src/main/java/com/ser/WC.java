package com.ser;

import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.global.GlobalScripting;

public class WC extends GlobalScripting {

    // Global script is applicable for global events
    // Example - put a message box when the user logs in to the system
    @Override
    public IBasicAction getStartUpAction() {
        IMessageAction msg = Doxis4ClassFactory.createShowMessageAction();
        msg.setCaption("Global Script");
        msg.setMessage("Global Scripting with Java OK 123!");
        return msg;
    }

}
