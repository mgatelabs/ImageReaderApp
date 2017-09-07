package com.mgatelabs.imagereaderapp.shared;

import java.util.List;

/**
 * Created by mmgat on 9/6/2017.
 */

public class StateTransfer {
    public String stateId;
    public List<String> screenIds;
    public List<PointTransfer> points;

    public StateTransfer () {

    }

    public String getStateId () {
        return stateId;
    }

    public void setStateId (String stateId) {
        this.stateId = stateId;
    }

    public List<String> getScreenIds () {
        return screenIds;
    }

    public void setScreenIds (List<String> screenIds) {
        this.screenIds = screenIds;
    }

    public List<PointTransfer> getPoints () {
        return points;
    }

    public void setPoints (List<PointTransfer> points) {
        this.points = points;
    }
}
