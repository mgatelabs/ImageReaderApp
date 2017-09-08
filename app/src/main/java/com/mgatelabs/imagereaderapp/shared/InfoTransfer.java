package com.mgatelabs.imagereaderapp.shared;

import java.util.Map;

/**
 * Created by mmgat on 9/6/2017.
 */

public class InfoTransfer {
    private Map<String, StateTransfer> states;

    private MapTransfer map;

    public Map<String, StateTransfer> getStates() {
        return states;
    }

    public void setStates(Map<String, StateTransfer> states) {
        this.states = states;
    }

    public MapTransfer getMap() {
        return map;
    }

    public void setMap(MapTransfer map) {
        this.map = map;
    }
}
