package edu.cmu.sei.mtdigraph;

import java.io.Serializable;
import java.util.ArrayList;

public class MTDigraphModeTransition implements ActiveContainer, Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = -2101644660378253539L;

	ArrayList<MTTaskGraph> taskTransitions = new ArrayList<MTTaskGraph>();

    MTDigraphMode sourceMode = null;
    MTDigraphMode targetMode = null;

    public MTDigraphModeTransition(MTDigraphMode sourceMode, MTDigraphMode targetMode){
        this.sourceMode = sourceMode;
        this.targetMode = targetMode;
    }

    public void addTaskTransition(MTTaskGraph g){
        taskTransitions.add(g);
        g.addParentMode(this);
    }

    public ArrayList<MTTaskGraph> getTaskTransitions() {
        return taskTransitions;
    }

    public void setSourceMode(MTDigraphMode s){
        sourceMode = s;
    }

    public void setTargetMode(MTDigraphMode t){
        targetMode = t;
    }

    boolean active=true;

    public void setActive(boolean a){
        active = a;
    }

    @Override
	public boolean isActive(){
        return active;
    }
}
