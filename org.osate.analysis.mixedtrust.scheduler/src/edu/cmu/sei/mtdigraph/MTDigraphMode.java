package edu.cmu.sei.mtdigraph;

import java.io.Serializable;
import java.util.ArrayList;

public class MTDigraphMode implements ActiveContainer, Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = -6918356954995359920L;

	ArrayList<MTTaskGraph> taskset = new ArrayList<MTTaskGraph>();

    public MTDigraphMode(){

    }

    boolean active=true;

    public void setActive(boolean a){
        active = a;
    }

    @Override
	public boolean isActive(){
        return active;
    }

    boolean transitioningOUt=false;

    public void setTransitioningOUt(boolean t){
        transitioningOUt = t;
    }

    public boolean isTransitioningOUt() {
        return transitioningOUt;
    }

    public void addTask(MTTaskGraph g){
        taskset.add(g);

        g.addParentMode(this);
    }

    public double getUtilization(){
        return 0.0;
    }

    public ArrayList<MTTaskGraph> getTaskset() {
        return taskset;
    }
}
