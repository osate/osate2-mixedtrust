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

public class MTTaskNode implements PriorityObject,Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = -3186316452112013898L;

	long deadline;
	long wcet;
	String name="";
	public String getName(){
		return name;
	}

	public void setName(String n){
		name=n;
	}


	long enforcementDeadline=0;

	public void setEnforcementDeadline(long e){
		enforcementDeadline = e;
	}

	public long getEnforcementDeadline(){
		return enforcementDeadline;
	}

	// This is either the HT of a GT or the GT of an HT to form the Mixed-Trust pair (GT,HT)
	MTTaskEdge mtPartnerEdge=null;

	public void setMtPartnerEdge(MTTaskEdge e){
		mtPartnerEdge = e;
	}

	public MTTaskEdge getMtPartnerEdge(){
		return  mtPartnerEdge;
	}

	int priority=0;

	boolean preemptible=true;

	boolean independentPriority = false;

	ArrayList<MTTaskGraph> parentGraphs=new ArrayList<MTTaskGraph>();

	public boolean isPreemptible(){
		return preemptible;
	}

	public void setPreemptible(boolean p){
		preemptible=p;
	}

	boolean transitioningHT = false;

	public void setTransitioningHT(boolean t){
		transitioningHT = t;
	}

	public boolean isTransitioningHT() {
		return transitioningHT;
	}

	long responseTime=0;

	public void setResponseTime(long r){
		responseTime = r;
	}

	public long getResponseTime(){
		return responseTime;
	}

	@Override
	public int getPriority(){
		if (!independentPriority && !parentGraphs.isEmpty()){
			return parentGraphs.get(0).getPriority();
		}
		return priority;
	}

	public void setPriority(int p){
		priority = p;
		independentPriority = true;
	}

	public void setDeadline(long d){
		deadline=d;
	}

	public long getDeadline(){
		if (isPreemptible()){
			return enforcementDeadline;
		}

		return deadline;
	}

	public long getWCET(){
		return wcet;
	}

	public MTTaskNode(boolean preempt, int p, long w, long d, String n){
		this(w,d,n);
		setPriority(p);
		preemptible = preempt;
	}

	public MTTaskNode(long w,long d, String n){
		this(w,d);
		name = n;
	}

	public MTTaskNode(long w,long d){
		deadline=d;
		enforcementDeadline = d;
		wcet=w;
	}

	@Override
	public String toString(){
		return getName()+"<C:"+getWCET()+",D:"+getDeadline()+">";
	}

	public void addParentGraph(MTTaskGraph mtTaskGraph) {
		parentGraphs.add(mtTaskGraph);
	}

	boolean active=false;

	public void setActive(boolean a){
		active =a;
	}

	public boolean isActive(){
		for (MTTaskGraph g:parentGraphs){
			if (g.isActive()) {
				return true;
			}
		}
		if (isTransitioningHT()) {
			return active;
		}

		return false;
	}

	public boolean hasCommonParent(MTTaskNode o){
		for (var p:parentGraphs){
			if (o.parentGraphs.contains(p)) {
				return true;
			}
		}
		return false;
	}

	static long nextUniqueId=0;
	long uniqueId = nextUniqueId++;

	@Override
	public long getUniqueId() {
		return uniqueId;
	}
}
