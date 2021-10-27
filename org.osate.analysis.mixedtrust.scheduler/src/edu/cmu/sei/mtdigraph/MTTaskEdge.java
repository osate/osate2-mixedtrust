/**
 * Mixed-Trust Scheduling Analysis OSATE Plugin
 *
 * Copyright 2021 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
 * AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR
 * PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF
 * THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF
 * ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT
 * INFRINGEMENT.
 *
 * Released under the Eclipse Public License - v 2.0 license, please see
 * license.txt or contact permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public
 * release and unlimited distribution.  Please see Copyright notice for
 * non-US Government use and distribution.
 *
 * Carnegie Mellon® is registered in the U.S. Patent and Trademark Office
 * by Carnegie Mellon University.
 *
 * DM21-0927
 */

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
