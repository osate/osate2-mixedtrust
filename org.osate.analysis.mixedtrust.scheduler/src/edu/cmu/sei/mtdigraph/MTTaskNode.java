/*******************************************************************************
 * Copyright (c) 2004-2021 Carnegie Mellon University and others. (see Contributors file).
 * All Rights Reserved.
 *
 * NO WARRANTY. ALL MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
 * MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Created, in part, with funding and support from the United States Government. (see Acknowledgments file).
 *
 * This program includes and/or can make use of certain third party source code, object code, documentation and other
 * files ("Third Party Software"). The Third Party Software that is used by this program is dependent upon your system
 * configuration. By using this program, You agree to comply with any and all relevant Third Party Software terms and
 * conditions contained in any such Third Party Software or separate license file distributed with such Third Party
 * Software. The parties who own the Third Party Software ("Third Party Licensors") are intended third party beneficiaries
 * to this license with respect to the terms applicable to their Third Party Software. Third Party Software licenses
 * only apply to the Third Party Software and not any other portion of this program or this program as a whole.
 *******************************************************************************/
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
