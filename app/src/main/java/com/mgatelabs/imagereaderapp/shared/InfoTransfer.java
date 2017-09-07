package com.mgatelabs.imagereaderapp.shared;

import java.util.Map;

/**
 * Created by mmgat on 9/6/2017.
 */

public class InfoTransfer {
    Map<String, StateTransfer> states;

    public Map<String, StateTransfer> getStates() {
        return states;
    }

    public void setStates(Map<String, StateTransfer> states) {
        this.states = states;
    }
}
