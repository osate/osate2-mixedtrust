package edu.cmu.sei.mtdigraph;

import java.io.Serializable;
import java.util.ArrayList;

public class MTTaskEdge implements Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = -914815620109729413L;

	long interArrival;
	MTTaskNode targetNode;
	MTTaskNode sourceNode;

	public MTTaskEdge(long i, MTTaskNode s, MTTaskNode t){
		interArrival = i;
		targetNode = t;
		sourceNode = s;
	}

	boolean activeDuringTransitionOut=false;

	ArrayList<MTTaskGraph> parentGraphs=new ArrayList<MTTaskGraph>();

	public void addParentGraph(MTTaskGraph mtTaskGraph) {
		parentGraphs.add(mtTaskGraph);
	}

	public boolean isActive(){
		for (MTTaskGraph g:parentGraphs){
			if (g.isActive()) {
				return true;
			}
		}
		return false;
	}

	public long getInterarrival(){
		return interArrival;
	}

	@Override
	public String toString(){
		return Long.toString(interArrival);
	}

	public void setInterarrival(long i){
		interArrival = i;
	}

	public MTTaskNode getTargetNode(){
		return targetNode;
	}

	public void setTargetNode (MTTaskNode n){
		targetNode = n;
	}

	public MTTaskNode getSourceNode(){
		return sourceNode;
	}

	public void setSourceNode(MTTaskNode n){
		sourceNode = n;
	}
}
