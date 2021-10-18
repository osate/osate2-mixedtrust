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
import java.util.HashMap;
import java.util.TreeSet;

public class MTTaskGraph implements PriorityObject, Comparable<MTTaskGraph> , Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = 4014183750174964874L;

	static long uniqueIdNext=0;

	long uniqueId = uniqueIdNext++;

	private ArrayList<ActiveContainer> parentModes=new ArrayList<ActiveContainer>();

	@Override
	public long getUniqueId(){
		return uniqueId;
	}

	int priority=0;

	public void setPriority(int p){
		priority = p;
	}

	@Override
	public int getPriority(){
		return priority;
	}

	// in increasing order of priority (smaller to larger)
	@Override
	public int compareTo(MTTaskGraph o) {
		if (o.getUniqueId() == getUniqueId()) {
			return 0;
		} else if (o.getPriority() == getPriority()) {
			return (int)(o.getUniqueId() - getUniqueId());
		} else if (o.getPriority() < getPriority()) {
			return 1;
		} else {
			return -1;
		}
	}

	ArrayList<MTTaskNode> nodes = new ArrayList<MTTaskNode>();
	HashMap<MTTaskNode,ArrayList<MTTaskEdge>> nodeToSuccessorEdges = new HashMap<MTTaskNode, ArrayList<MTTaskEdge>>();
	HashMap<MTTaskNode,ArrayList<MTTaskEdge>> nodeToPredecessorEdges = new HashMap<MTTaskNode, ArrayList<MTTaskEdge>>();

	public ArrayList<MTTaskEdge> getSuccessorEdges(MTTaskNode n){
		return nodeToSuccessorEdges.get(n);
	}

	public ArrayList<MTTaskEdge> getPredecessorEdges(MTTaskNode n){
		return nodeToPredecessorEdges.get(n);
	}


	String name="";

	public String getName(){
		return name;
	}

	public MTTaskGraph(){
	}

	public MTTaskGraph(String n){
		name = n;
	}

	public void addNode(MTTaskNode n){
		maxJitter = -1;
		nodes.add(n);
		n.addParentGraph(this);
	}

	public ArrayList<MTTaskNode> getNodes(){
		return nodes;
	}
	public void addEdge(MTTaskEdge e){
		maxJitter = -1;
		ArrayList<MTTaskEdge> succEdges = nodeToSuccessorEdges.get(e.getSourceNode());
		if (succEdges == null){
			succEdges = new ArrayList<MTTaskEdge>();
			nodeToSuccessorEdges.put(e.getSourceNode(), succEdges);
		}
		succEdges.add(e);

		ArrayList<MTTaskEdge> predEdges = nodeToPredecessorEdges.get(e.getTargetNode());
		if (predEdges == null){
			predEdges = new ArrayList<MTTaskEdge>();
			nodeToPredecessorEdges.put(e.getTargetNode(), predEdges);
		}
		predEdges.add(e);

		e.addParentGraph(this);
	}

	public long getMaxRequestBound(boolean inclusive, MTTaskNode victim, MTTaskNode n, long interval, ArrayList<MTTaskEdge> path) {
		long l = 0;
		long r = 0;
		MTTaskEdge nextEdge = null;
		ArrayList<MTTaskEdge> newpath = null;
		var succs = nodeToSuccessorEdges.get(n);
		if (succs != null){
			for (MTTaskEdge e : succs) {
				// skip inactive edges
				if (!e.isActive()) {
					continue;
				}

				// still within interval?

				// TODO: checking for an inclusive interval is an overapproximation
				// if the tasks have only preemptible nodes. However, it is not straightforward
				// to figure out the combination that will allow an exclusive interval
				if ( e.getInterarrival() < interval || (inclusive && e.getInterarrival() == interval)){
					//System.out.println("Interval("+interval+"): Traversing: "+e.getSourceNode().name+"-["+e.getInterarrival()+"]->"+e.getTargetNode().name);
					ArrayList<MTTaskEdge> tmppath = null;
					if (path != null) {
						tmppath = new ArrayList<MTTaskEdge>();
					}
					r = getMaxRequestBound(inclusive, victim, e.getTargetNode(), interval - e.getInterarrival(), tmppath);
					if (r > l) {
						l = r;
						nextEdge = e;
						newpath = tmppath;
					}
				} else {
					//System.out.println("\t UNREACHABLE edge("+e.getSourceNode().name+"->"+e.getTargetNode().name+").Interarrival("+e.getInterarrival()+") > interval("+interval+")");
				}
			}
		}

		if (nextEdge != null && path != null){
			path.add(nextEdge);
			path.addAll(newpath);
		}
		if (n.getPriority() > victim.getPriority() || !n.isActive()){
			// victim node has higher priority hence the execution is zero
			return l;
		} else {
			if (victim.hasCommonParent(n) || MTDigraphScheduler.accounted.contains(n)) {
				//MTDigraphScheduler.trace.add("vitim("+victim+").RF(t:"+interval+",w/o g("+this+").node("+n+"),rf:"+l+")");
				return l;
			} else {
				MTDigraphScheduler.trace.add("vitim("+victim+").RF(t:"+interval+",g("+this+").node("+n+"),rf:"+(l+n.getWCET())+")");
				return l + n.getWCET();
			}
		}
	}


	// TODO: eliminate this method and substitute for the common one that passes a boolean inclusive parameter above.
	public long getMaxRequestBoundInclusive(MTTaskNode victim, MTTaskNode n, long interval, ArrayList<MTTaskEdge> path) {
		long l = 0;
		long r = 0;
		MTTaskEdge nextEdge = null;
		ArrayList<MTTaskEdge> newpath = null;
		var succs = nodeToSuccessorEdges.get(n);
		if (succs != null){
			for (MTTaskEdge e : succs) {
				// still within interval?

				// TODO: checking for an inclusive interval is an overapproximation
				// if the tasks have only preemptible nodes. However, it is not straightforward
				// to figure out the combination that will allow an exclusive interval
				if (e.getInterarrival() <= interval) {
					//System.out.println("Interval("+interval+"): Traversing: "+e.getSourceNode().name+"-["+e.getInterarrival()+"]->"+e.getTargetNode().name);
					ArrayList<MTTaskEdge> tmppath = null;
					if (path != null) {
						tmppath = new ArrayList<MTTaskEdge>();
					}
					r = getMaxRequestBoundInclusive(victim, e.getTargetNode(), interval - e.getInterarrival(), tmppath);
					if (r > l) {
						l = r;
						nextEdge = e;
						newpath = tmppath;
					}
				} else {
					//System.out.println("\t UNREACHABLE edge("+e.getSourceNode().name+"->"+e.getTargetNode().name+").Interarrival("+e.getInterarrival()+") > interval("+interval+")");
				}
			}
		}

		if (nextEdge != null && path != null){
			path.add(nextEdge);
			path.addAll(newpath);
		}
		if (n.getPriority() > victim.getPriority()){
			// victim node has higher priority hence the execution is zero
			return l;
		} else {
			return l + n.getWCET();
		}
	}

	// TODO: eliminate this method and substitute for the common one that passes a boolean inclusive parameter above.
	public long getMaxRequestBoundExclusive(MTTaskNode victim, MTTaskNode n, long interval, ArrayList<MTTaskEdge> path){
		long l=0;
		long r=0;
		MTTaskEdge nextEdge=null;
		ArrayList<MTTaskEdge> newpath=null;
		for (MTTaskEdge e: nodeToSuccessorEdges.get(n)){
			// still within interval?
			if (e.getInterarrival()<interval){
				//System.out.println("Interval("+interval+"): Traversing: "+e.getSourceNode().name+"-["+e.getInterarrival()+"]->"+e.getTargetNode().name);
				ArrayList<MTTaskEdge> tmppath = null;
				if (path != null){
					tmppath = new ArrayList<MTTaskEdge>();
				}
				r = getMaxRequestBoundExclusive(victim, e.getTargetNode(), interval - e.getInterarrival(),tmppath);
				if (r>l){
					l = r;
					nextEdge = e;
					newpath=tmppath;
				}
			} else {
				//System.out.println("\t UNREACHABLE edge("+e.getSourceNode().name+"->"+e.getTargetNode().name+").Interarrival("+e.getInterarrival()+") > interval("+interval+")");
			}
		}

		if (nextEdge != null && path != null){
			path.add(nextEdge);
			path.addAll(newpath);
		}
		if (n.getPriority()<victim.getPriority()) {
			return l + n.getWCET();
		} else {
			return l;
		}
	}

	public static void printPath(ArrayList<MTTaskEdge> path){
		Boolean first = true;
		long cuminter=0;
		long cumwcet=0;

		System.out.println(" ---- PATH ----");
		for (MTTaskEdge e:path){
			if (first){
				first = false;
				System.out.println("\n"+e.getSourceNode());
				cumwcet += e.getSourceNode().getWCET();
			}
			System.out.println("-["+e.getInterarrival()+"]->"+e.getTargetNode());
			cuminter += e.getInterarrival();
			cumwcet += e.getTargetNode().getWCET();
			System.out.println("\t cumWcet("+cumwcet+") cumInter("+cuminter+")");
		}
		System.out.println(" ---- END PATH ----\n");
	}

	long maxJitter = -1;

	public long getMaximumJitter(MTTaskNode victim){
		maxJitter = 0;
		MTTaskNode maxJitterNode=null;
		//if (maxJitter <0){
			for (MTTaskNode node:nodes){
				if (node.hasCommonParent(victim)) {
					continue;
				}
				if (!node.isActive()) {
					continue;
				}
				if (victim.getPriority() > node.getPriority()) {
					if (node.getDeadline() - node.getWCET() > maxJitter) {
						maxJitter = node.getDeadline() - node.getWCET();
						maxJitterNode = node;
					}
				}
			}
		//}
		if (maxJitterNode != null) {
			MTDigraphScheduler.trace.add("victim("+victim+").jitter("+maxJitter+","+maxJitterNode+")");
		}
		return maxJitter;
	}

	public long getNonPreemptiveBlocking(MTTaskNode victim) {
		long b=0;
		for (MTTaskNode node : nodes) {
			// this only apply for the non-preemptible hypertasks
			// because a lower priority guest task cannot interfere with
			// a hypertask

			// this can only be tested at the node level not the graph level
			if (victim.hasCommonParent(node)) {
				continue;
			}

			if (node.isPreemptible()) {
				continue;
			}
			if (!node.isActive()) {
				continue;
			}
			if (node.getPriority()>victim.getPriority() && node.getWCET()>b){
				MTDigraphScheduler.trace.add("vitim("+victim+").block("+node+")");
				b = node.getWCET();
			}
		}
		return b;
	}

	public long getPrefixedRequestBound(MTTaskNode victim, long interval, ArrayList<MTTaskEdge> path, long currentCummulativeRequest){
		long maxl=0;
		ArrayList<MTTaskEdge> maxPath=null;
		ArrayList<MTTaskEdge>tmpPath=null;
		@SuppressWarnings("unused")
		MTTaskNode rootLongest=null;
		if (path != null) {
			tmpPath = new ArrayList<MTTaskEdge>();
		}

		// Enlarge interval with maximum task-wise jitter
		// to overapproximate the path prefix interference
		interval += getMaximumJitter(victim);

		// make sure that we are not getting interference
		// from the same parent
		if (victim.parentGraphs.contains(this)) {
			return 0;
		}

		for (MTTaskNode node:nodes){
			long l = 0;
			if (victim == node) {
				continue;
			}
			if (victim.hasCommonParent(node)) {
				continue;
			}

			if (!victim.isPreemptible()) {
				l = getMaxRequestBound(true, victim, node, interval, tmpPath);
			} else {
				l = getMaxRequestBound(false, victim, node, interval, tmpPath);
			}

			if (l>maxl){
				if (path != null) {
					maxPath = tmpPath;
					tmpPath = new ArrayList<MTTaskEdge>();
				}
				maxl = l;
			}

			// debugging
			MTDigraphScheduler.logDebugMessage(currentCummulativeRequest + maxl + victim.getWCET() > victim.getDeadline() && !victim.isPreemptible(),
					new LogBuilder(new Object[]{interval,maxl}) {
						@Override
						public String buildLog() {
							String str="";
							str += MTDigraphScheduler.traceToString();
							str += "victim("+victim+"): cumm("+currentCummulativeRequest+")+rf("+parms[0]+"):"+parms[1]+"C("+victim.getWCET()+")>deadline("+victim.getDeadline()+")\n";
							return str;
						}
					});
//			if (currentCummulativeRequest+maxl+victim.getWCET() > victim.getDeadline() && !victim.isPreemptible()) {
//				System.out.print(MTDigraphScheduler.traceToString());
//				System.out.println("victim("+victim+"): cumm("+currentCummulativeRequest+")+rf("+interval+"):"+maxl+"C("+victim.getWCET()+")>deadline("+victim.getDeadline()+")\n");
//				break;
//			}

			MTDigraphScheduler.logDebugMessage(currentCummulativeRequest + maxl > victim.getDeadline() && victim.isPreemptible(),
					new LogBuilder(new Object[]{interval,maxl}) {
						@Override
						public String buildLog() {
							String str="";
							str += MTDigraphScheduler.traceToString();
							str += "victim("+victim+"):cumm("+currentCummulativeRequest+")+rf("+parms[0]+"):"+parms[1]+">deadline("+victim.getDeadline()+")\n";
							return str;
						}
					});
//			if (currentCummulativeRequest+maxl > victim.getDeadline() && victim.isPreemptible()){
//				System.out.print(MTDigraphScheduler.traceToString());
//				System.out.println("victim("+victim+"):cumm("+currentCummulativeRequest+")+rf("+interval+"):"+maxl+">deadline("+victim.getDeadline()+")\n");
//				break;
//			}

		}

		if (path != null && maxPath != null) {
			path.addAll(maxPath);
		}
		return maxl;
	}

	public long getRequestBound(MTTaskNode n0, long interval, ArrayList<MTTaskEdge> path){
		long maxl=0;
		ArrayList<MTTaskEdge> maxPath=null;
		ArrayList<MTTaskEdge>tmpPath=null;
		@SuppressWarnings("unused")
		MTTaskNode rootLongest=null;
		if (path != null) {
			tmpPath = new ArrayList<MTTaskEdge>();
		}

		for (MTTaskNode node:nodes){
			long l = getMaxRequestBoundExclusive(n0,node, interval, tmpPath);

			if (l>maxl){
				if (path != null) {
					maxPath = tmpPath;
					tmpPath = new ArrayList<MTTaskEdge>();
				}
				maxl = l;
			}
		}

		if (path != null) {
			path.addAll(maxPath);
		}
		return maxl;
	}

	// test

	public static void testTaskGraphRTSS2013Fig1(){
		MTTaskGraph g0 = new MTTaskGraph();
		MTTaskNode n0 = new MTTaskNode(1,10,"n0");
		g0.addNode(n0);

		g0.setPriority(1);


		MTTaskGraph g1 = new MTTaskGraph();
		g1.setPriority(0);

		MTTaskNode n1 = new MTTaskNode(6,10,"n1");
		MTTaskNode n2 = new MTTaskNode(5,25,"n2");
		MTTaskNode n3 = new MTTaskNode(1,10,"n3");
		MTTaskNode n4 = new MTTaskNode(2,12,"n4");
		MTTaskNode n5 = new MTTaskNode(10,50,"n5");

		g1.addNode(n1);
		g1.addNode(n2);
		g1.addNode(n3);
		g1.addNode(n4);
		g1.addNode(n5);

		g1.addEdge(new MTTaskEdge(13,n1,n2));
		g1.addEdge(new MTTaskEdge(100,n2,n4));
		g1.addEdge(new MTTaskEdge(29,n2,n3));
		g1.addEdge(new MTTaskEdge(10,n3,n4));
		g1.addEdge(new MTTaskEdge(18,n3,n5));
		g1.addEdge(new MTTaskEdge(12,n4,n1));
		g1.addEdge(new MTTaskEdge(25,n4,n2));
		g1.addEdge(new MTTaskEdge(50,n5,n5));

		ArrayList<MTTaskEdge>path= new ArrayList<MTTaskEdge>();

		System.out.println("Request Bound: "+g1.getRequestBound(n0,200,path));

		System.out.print("Path:");

		printPath(path);

		ArrayList<MTTaskGraph> taskset = new ArrayList<MTTaskGraph>();
		taskset.add(g1);
	}

	static ArrayList<MTTaskGraph> testTaskset = new ArrayList<MTTaskGraph>();

	public static ArrayList<MTTaskGraph> getTestTaskset(){
		return  testTaskset;
	}

	public static void testTaskGraphsRTJ20015Fig12(){
		MTTaskGraph g1 = new MTTaskGraph();

		MTTaskNode u = new MTTaskNode(5,7,"u");
		u.setPriority(1);
		MTTaskNode v = new MTTaskNode(5,10, "v");
		v.setPriority(3);

		MTTaskEdge uv = new MTTaskEdge(7,u,v);

		g1.addNode(u);
		g1.addNode(v);
		g1.addEdge(uv);

		MTTaskGraph g2 = new MTTaskGraph();

		MTTaskNode w = new MTTaskNode(4,10,"w");
		w.setPriority(2);

		MTTaskEdge ww = new MTTaskEdge(10,w,w);

		g2.addNode(w);
		g2.addEdge(ww);

		ArrayList<MTTaskEdge>path= new ArrayList<MTTaskEdge>();

		System.out.println("PrefixedRequestBound: "+g2.getPrefixedRequestBound(v, v.getWCET(), path, 0));

		printPath(path);
		testTaskset.add(g2);
		testTaskset.add(g1);

		//MTDigraphVisualizer visualizer = new MTDigraphVisualizer(testTaskset);
		//visualizer.show();

	}

	public static void testComparator(){
		MTTaskGraph g1 = new MTTaskGraph();
		g1.setPriority(1);
		MTTaskGraph g2 = new MTTaskGraph();
		g2.setPriority(2);

		TreeSet<MTTaskGraph> set = new TreeSet<>();
		set.add(g2);
		set.add(g1);

		for (MTTaskGraph g:set){
			System.out.println("prio("+g.getPriority()+")");
		}
	}

	public static void main(String[] args){
		//testTaskGraphRTSS2013Fig1();
		//testComparator();
		testTaskGraphsRTJ20015Fig12();
	}

	public void addParentMode(ActiveContainer mtDigraphMode) {
		parentModes.add(mtDigraphMode);
	}

	public boolean isActive(){
		if (parentModes.isEmpty()) {
			return true;
		}
		for (ActiveContainer m:parentModes){
			if (m.isActive()) {
				return true;
			}
		}
		return false;
	}
}
