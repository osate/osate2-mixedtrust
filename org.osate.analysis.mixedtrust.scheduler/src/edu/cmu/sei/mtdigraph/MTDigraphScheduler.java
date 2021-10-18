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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import edu.cmu.sei.mtzsrm.LayeredTrustExactScheduler;

public class MTDigraphScheduler {
	TreeSet<MTTaskGraph> taskset = new TreeSet<MTTaskGraph>();
	long largestBlocking=0;

	public static ArrayList<String> trace=new ArrayList<String>();

	public static String traceToString(){
		String r="";
		for (String s:trace){
			r+=s+"\n";
		}
		return r;
	}

	public TreeSet<MTTaskGraph> getTaskset(){
		return taskset;
	}

	public void addTask(MTTaskGraph g){
		taskset.add(g);
	}

	public long getNonPreemptibleBlocking(MTTaskGraph gvictim, MTTaskNode nvictim){
		long b=0,tmpb=0;
		for (MTTaskGraph og: taskset){
			// we will check if isActive at the node level
			// to allow a transitioning graph to be inactive but its
			// transitioning hypertask to be active

			//if (!og.isActive())
			//	continue;

			if (nvictim.parentGraphs.contains(og)) {
				continue;
			}

			tmpb=og.getNonPreemptiveBlocking(nvictim);
			if (tmpb>b) {
				b=tmpb;
			}
		}
		return b;
	}

	public static void assignDeadlineMonotonicPriorities(TreeSet<MTTaskNode> nodes){
		// first traverse the non-Preemptible
		int priority=1;
		for (var n:nodes){
			if (n.isPreemptible()) {
				continue;
			}
			n.setPriority(priority++);
		}
		// now the preemptible
		for (var n:nodes) {
			if (!n.isPreemptible()) {
				continue;
			}
			n.setPriority(priority++);
		}
	}

	public boolean isMTModalSystemSchedulable(MTDigraphModalSystem s){
		trace.clear();
		taskset.clear();

		// start with deadline monotonic priority assignment
		var nodes = new TreeSet<MTTaskNode>(new IncreasingDeadlineComparator());
		for (var m:s.getModes()){
			for (var ts:m.getTaskset()) {
				nodes.addAll(ts.getNodes());
			}
		}
		for (var trans:s.getSystemTransitions()){
			for (var g:trans.getTaskTransitions()){
				nodes.addAll(g.getNodes());
			}
		}

		assignDeadlineMonotonicPriorities(nodes);

		// Given that we now have nodes that may belong to more than one graph, e.g., both to a mode
		// and a transition then we will test for schedulability each node multiple times
		// This can be solved by having a "tested" set to prevent this.

		// add all modes tasks
		for (MTDigraphMode m:s.getModes()){
			taskset.addAll(m.getTaskset());
		}

		// and all transitions
		for (MTDigraphModeTransition t:s.getSystemTransitions()){
			taskset.addAll(t.getTaskTransitions());
		}

		// deactivate all modes
		for (MTDigraphMode m:s.getModes()){
			m.setActive(false);
		}
		// and all transitions
		for (MTDigraphModeTransition t:s.getSystemTransitions()){
			t.setActive(false);
		}

		// to preserve the minimumDeadline
		var maxResponseTime = new HashMap<MTTaskNode,Long> ();

		// explore all modes hypertasks.
		for (MTDigraphMode m:s.getModes()) {
			m.setActive(true);
			for (MTTaskGraph g:m.getTaskset()){
				if (!isHTSchedulable(g)){
					return false;
				}
				for (MTTaskNode n:g.getNodes()){
					if (n.isPreemptible()) {
						continue;
					}
					Long r = maxResponseTime.get(n);
					if (r == null){
						maxResponseTime.put(n,n.getResponseTime());
					} else if(r < n.getResponseTime()) {
						maxResponseTime.put(n,n.getResponseTime());
					}
				}
			}
			m.setActive(false);
		}

		for (MTDigraphModeTransition t:s.getSystemTransitions()) {
			t.setActive(true);

			for (MTTaskGraph g:t.getTaskTransitions()){
				if (!isHTSchedulable(g)) {
					return false;
				}
				for (MTTaskNode n:g.getNodes()){
					if (n.isPreemptible()) {
						continue;
					}
					Long r = maxResponseTime.get(n);
					if (r == null){
						maxResponseTime.put(n,n.getResponseTime());
					} else if(r < n.getResponseTime()) {
						maxResponseTime.put(n,n.getResponseTime());
					}
				}
			}
			t.setActive(false);
		}

		// now i have the maximum R for all nodes
		// set it up

		for (MTDigraphMode m:s.getModes()) {
			for (MTTaskGraph g : m.getTaskset()) {
				for (MTTaskNode n : g.getNodes()) {
					if (n.isPreemptible()) {
						continue;
					}
					n.setResponseTime(maxResponseTime.get(n));
				}
			}
		}

		for (MTDigraphModeTransition t:s.getSystemTransitions()) {
			for (MTTaskGraph g : t.getTaskTransitions()) {
				for (MTTaskNode n : g.getNodes()) {
					if (n.isPreemptible()) {
						continue;
					}
					n.setResponseTime(maxResponseTime.get(n));
				}
			}
		}

		// now it is necessary to find the maximum response time between the source HT and the transitioning HT

		var adjustedDeadlineNodes = new ArrayList<MTTaskNode>();
		for (MTDigraphModeTransition t:s.getSystemTransitions()) {
			for (MTTaskGraph g : t.getTaskTransitions()) {
				for (MTTaskNode tHT : g.getNodes()) {
					if (tHT.isTransitioningHT()){
						MTTaskEdge inE=null;
						for (MTTaskEdge e:g.getPredecessorEdges(tHT)) {
							if (e.getSourceNode() != e.getTargetNode()){
								inE = e;
								break;
							}
						}
						MTTaskEdge outE=null;
						for (MTTaskEdge e:g.getSuccessorEdges(tHT)) {
							if (e.getSourceNode() != e.getTargetNode()) {
								outE = e;
								break;
							}
						}
						if (inE != null){
							MTTaskNode sHT = inE.getSourceNode().getMtPartnerEdge().getTargetNode();
							MTTaskNode sGT = inE.getSourceNode();

							Long maxR = tHT.getResponseTime() > sHT.getResponseTime() ? tHT.getResponseTime() : sHT.getResponseTime();
							tHT.setDeadline(maxR);
							sGT.setEnforcementDeadline(sGT.getDeadline()-tHT.getDeadline());
							outE.setInterarrival(tHT.getDeadline());
							sHT.setDeadline(maxR);
							sHT.getMtPartnerEdge().setInterarrival(sHT.getDeadline());
							inE.setInterarrival(sGT.getEnforcementDeadline());
							sGT.getMtPartnerEdge().setInterarrival(sGT.getEnforcementDeadline());

							adjustedDeadlineNodes.add(tHT);
							adjustedDeadlineNodes.add(sHT);
							adjustedDeadlineNodes.add(sGT);
						}
					}
				}
			}
		}

		for (MTDigraphMode m:s.getModes()) {
			for (MTTaskGraph g : m.getTaskset()) {
				for (MTTaskNode ht : g.getNodes()) {
					if (ht.isPreemptible()) {
						continue;
					}
					if (!adjustedDeadlineNodes.contains(ht)){
						adjustedDeadlineNodes.add(ht);
						if (ht.getMtPartnerEdge() != null){
							// get nodes and edges
							MTTaskEdge ht2gtEdge = ht.getMtPartnerEdge();
							MTTaskNode gt = ht2gtEdge.getTargetNode();
							MTTaskEdge gt2htEdge = gt.getMtPartnerEdge();

							// set deadlines and interarrivals
							ht.setDeadline(ht.getResponseTime());
							ht2gtEdge.setInterarrival(ht.getDeadline());
							gt.setEnforcementDeadline(gt.getDeadline()-ht.getResponseTime());
							gt2htEdge.setInterarrival(gt.getEnforcementDeadline());
						}
					}
				}
			}
		}

					// now we are ready to test the schedulability of all GTs

		for (MTDigraphMode m:s.getModes()) {
			m.setActive(true);
			for (MTTaskGraph g:m.getTaskset()){
				if (!isGTSchedulable(g)){
					return false;
				}
			}
			m.setActive(false);
		}

		for (MTDigraphModeTransition t:s.getSystemTransitions()) {
			t.setActive(true);
			for (MTTaskGraph g:t.getTaskTransitions()){
				if (!isGTSchedulable(g)) {
					return false;
				}
			}
			t.setActive(false);
		}


		return true;
	}


	public boolean isMTModalSystemSchedulableOld(MTDigraphModalSystem s){


		trace.clear();
		taskset.clear();

		// Given that we now have nodes that may belong to more than one graph, e.g., both to a mode
		// and a transition then we will test for schedulability each node multiple times
		// This can be solved by having a "tested" set to prevent this.

		// add all modes tasks
		for (MTDigraphMode m:s.getModes()){
			taskset.addAll(m.getTaskset());
		}
		// and all transitions
		for (MTDigraphModeTransition t:s.getSystemTransitions()){
			taskset.addAll(t.getTaskTransitions());
		}

		// calculate the largest blocking
		largestBlocking = 0;

		for (MTTaskGraph g:taskset){
			for (MTTaskNode n:g.getNodes()){
				if (n.isPreemptible()) {
					continue;
				}
				if (largestBlocking<n.getWCET()){
					largestBlocking = n.getWCET();
				}
			}
		}


		// deactivate all modes
		for (MTDigraphMode m:s.getModes()){
			m.setActive(false);
		}
		// and all transitions
		for (MTDigraphModeTransition t:s.getSystemTransitions()){
			t.setActive(false);
		}

		// TODO: try activating all the HT of the "source" mode
		// only the modes that have a transition into the mode I'll
		// be testing
		// how do we know that they have the proper priority


		// but activate all transitioning HT
		for (MTDigraphModeTransition t:s.getSystemTransitions()){
			for (MTTaskGraph g:t.getTaskTransitions()){
				for (MTTaskNode n:g.getNodes()){
					if (n.isTransitioningHT()){
						n.setActive(true);
						break;
					}
				}
			}
		}

		// explore all modes
		for (MTDigraphMode m:s.getModes()) {
			m.setActive(true);
			for (MTTaskGraph g:m.getTaskset()){
				if (!isMTSchedulable(g,true)){
					return false;
				}
			}
			m.setActive(false);
		}

		// return transitioning HT to inactive
		for (MTDigraphModeTransition t:s.getSystemTransitions()){
			for (MTTaskGraph g:t.getTaskTransitions()){
				for (MTTaskNode n:g.getNodes()){
					if (n.isTransitioningHT()){
						n.setActive(false);
						break;
					}
				}
			}
		}


		for (MTDigraphModeTransition t:s.getSystemTransitions()) {
			t.setActive(true);
			// TODO: we activate the target mode just in case there are new tasks that had not been explored
			// we probably should do something similar for the source mode.
			// Perhaps the best alternative is to create an independent graph that is added to the transitioning
			// graphs with only the active parts of the graph

			// propagate the deadline adjustments to interarrivals of the transitioning tasks
			for (MTTaskGraph g:t.getTaskTransitions()) {
				for (MTTaskNode ht:g.getNodes()){
					if (ht.isTransitioningHT()){
						MTTaskEdge inE = null, outE = null; // ,selfE=null;
						for (MTTaskEdge e:g.getSuccessorEdges(ht)){
							if (e.getTargetNode() == e.getSourceNode()){
//								selfE = e;
							} else {
								outE = e;
							}
						}
						for (MTTaskEdge e:g.getPredecessorEdges(ht)) {
							if (e.getSourceNode() != e.getTargetNode()){
								inE = e;
							}
						}

						inE.setInterarrival(inE.getSourceNode().getEnforcementDeadline());
						//outE.setInterarrival(selfE.getInterarrival() - inE.getSourceNode().getEnforcementDeadline());
						outE.setInterarrival(ht.getEnforcementDeadline());
						break;
					}
				}
			}

			// reset priority to deadline monotonic
			var nodes = new TreeSet<MTTaskNode>(new IncreasingDeadlineComparator());
			for (var m:s.getModes()){
				for (var ts:m.getTaskset()) {
					nodes.addAll(ts.getNodes());
				}
			}
			for (var trans:s.getSystemTransitions()){
				for (var g:trans.getTaskTransitions()){
					nodes.addAll(g.getNodes());
				}
			}

			assignDeadlineMonotonicPriorities(nodes);

			// TODO: for now I am commenting out the target mode
			//t.targetMode.setActive(true);
			for (MTTaskGraph g:t.getTaskTransitions()){
				if (!isMTSchedulable(g,false)) {
					return false;
				}
			}
			/*
			for (MTTaskGraph g:t.targetMode.getTaskset()) {
				if (!isMTSchedulable(g,false)) {
					return false;
				}
			}
			 */
			t.setActive(false);
			//t.targetMode.setActive(false);
		}

		return true;
	}

	public boolean isHTSchedulable(MTTaskGraph g){
		for (MTTaskNode n : g.nodes) {
			if (n.isPreemptible()) {
				continue;
			}
			if (!n.isActive()) {
				continue;
			}
			if (!isMTNodeSchedulable(g,n)) {
				return false;
			}
		}
		return true;
	}

	public boolean isGTSchedulable(MTTaskGraph g){
		for (MTTaskNode n : g.nodes) {
			if (!n.isPreemptible()) {
				continue;
			}
			if (!n.isActive()) {
				continue;
			}
			if (!isMTNodeSchedulable(g,n)) {
				return false;
			}
		}
		return true;
	}

	public boolean isMTSchedulable(MTTaskGraph g, boolean adjustDeadlines) {

		// first schedule the non-preemptive (HT) tasks
		for (MTTaskNode n : g.nodes) {
			if (n.isPreemptible()) {
				continue;
			}
			if (!n.isActive()) {
				continue;
			}
			if (!isMTNodeSchedulable(g,n)) {
				return false;
			}
		}

		// now set the enforcement deadline
		for (MTTaskNode n : g.nodes) {
			if (!n.isPreemptible()) {
				continue;
			}

			if (adjustDeadlines) {
				n.setEnforcementDeadline(n.getDeadline() - n.getMtPartnerEdge().getTargetNode().getResponseTime()-largestBlocking);
				n.getMtPartnerEdge().setInterarrival(n.getEnforcementDeadline());
				long D = n.getMtPartnerEdge().getTargetNode().getDeadline();
				n.getMtPartnerEdge().getTargetNode().setDeadline(D - n.getEnforcementDeadline());
				n.getMtPartnerEdge().getTargetNode().getMtPartnerEdge().setInterarrival(n.getMtPartnerEdge().getTargetNode().getDeadline());
			}
		}

		// now schedule the preemptive (GT) tasks
		for (MTTaskNode n : g.nodes) {
			if (!n.isPreemptible()) {
				continue;
			}
			if (!n.isActive()) {
				continue;
			}
			if (!isMTNodeSchedulable(g,n)) {
				return false;
			}
		}

		return true;
	}

	public static ArrayList<MTTaskNode> accounted=new ArrayList<MTTaskNode>();

	public boolean isMTNodeSchedulable(MTTaskGraph g, MTTaskNode n) {
		long rp = 0, r = n.getWCET(), blocking;

		while (r != rp && rp <= n.getDeadline()) {
			r = rp;
			MTDigraphScheduler.trace.clear();
			// if it is preemptible we check for the completion
			// otherwise we check for the starting time
			if (n.isPreemptible()) {
				rp = n.getWCET();
			} else {
				blocking = getNonPreemptibleBlocking(g, n);
				rp=blocking;
			}

			accounted.clear();
			for (MTTaskGraph o : taskset) {
				// we will check whether a node is active instead of the full graph
				// to allow transitioning HT to be active even if its graph is
				// inactive
				//if (!o.isActive())
				//	continue;
				if (n.parentGraphs.contains(o)) {
					continue;
				}


				var lpath = new ArrayList<MTTaskEdge>();
				rp += o.getPrefixedRequestBound(n, r, lpath, rp);

				accounted.addAll(o.getNodes());

				// debugging
				MTDigraphScheduler.logDebugMessage(rp + n.getWCET() > n.getDeadline() && !n.isPreemptible() || rp  > n.getDeadline(),
						new LogBuilder(new Object[]{r,rp,lpath},"sched-path") {
							@Override
							public String buildLog() {
								@SuppressWarnings("unchecked")
								var lpath = (ArrayList<MTTaskEdge>) parms[2];
								String str="";
								boolean first=true;
								for (MTTaskEdge e:lpath){
									if (first){
										str+="n("+e.getSourceNode()+")";
										first = false;
									}
									str+="--["+e+"]-->n("+e.getTargetNode()+")";
								}
								str+="\n";
								return str;
							}
						});
				MTDigraphScheduler.logDebugMessage(rp > n.getDeadline() && n.isPreemptible(),
						new LogBuilder(new Object[]{r,rp,lpath},"sched") {
							@Override
							public String buildLog() {
								@SuppressWarnings("unchecked")
								var lpath = (ArrayList<MTTaskEdge>)parms[2];
								String str = "";
								boolean first = true;
								str += "victim(" + n + "),rf(" + parms[0] + "):" + parms[1] + " > deadline(" + n.getDeadline() + ")";
								for (MTTaskEdge e : lpath) {
									if (first) {
										str += "n(" + e.getSourceNode() + ")";
										first = false;
									}
									str += "--[" + e + "]-->n(" + e.getTargetNode() + ")";
								}
								str += "\n";
								return str;
							}
						});
				}

			}

		if (rp <= n.getDeadline()) {
			// if it is not preemptible rp is the starting time and we need to
			// add the node execution time
			if (!n.isPreemptible()) {
				rp += n.getWCET();
			}
			if (rp > n.getDeadline()) {
				return false;
			} else {
				n.setResponseTime(rp);
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean isSchedulable(MTTaskGraph g){
		for (MTTaskNode n:g.nodes){
			long rp=n.getWCET(),r=0,rb=0;

			while (r != rp && rp<= n.getDeadline()){
				r = rp;

				rp = n.getWCET();
				rb=0;

				for (MTTaskGraph o:taskset){
					if (o.getPriority()>g.getPriority()){
						continue;
					}
					//double check /simplify
					if(o.getUniqueId() == g.getUniqueId()) {
						continue;
					}

					rb += o.getRequestBound(n, r, null);
				}
				rp += rb;
			}

			if (rp> n.getDeadline()) {
				return false;
			}
		}

		return true;
	}

	public boolean isTasksetMTSchedulable(){

		for(MTTaskGraph g:taskset){
			if (!isMTSchedulable(g,false)){
				return false;
			}
		}
		return true;
	}


	public boolean isTasksetSchedulable(){

		// initialize priorities
		int i=0;
		int lowestPriority;

		for (MTTaskGraph g:taskset){
			g.setPriority(i++);
		}
		lowestPriority = i;

		ArrayList<MTTaskGraph> unscheduled = new ArrayList<MTTaskGraph>();
		unscheduled.addAll(taskset);

		i=0;

		while (unscheduled.size()>0){
		    boolean foundSchedulable = false;
			for (i=0;i<unscheduled.size();i++){
			    int prevPrio;
				MTTaskGraph g = unscheduled.get(i);
				prevPrio = g.getPriority();
				g.setPriority(lowestPriority);
				if (isSchedulable(g)){
					unscheduled.remove(g);
					lowestPriority--;
					foundSchedulable = true;
					break;
				} else {
				    g.setPriority(prevPrio);
                }
			}
			if (!foundSchedulable){
			    break;
            }
		}

		return unscheduled.isEmpty();
	}

	static boolean debugging = false;

	static int debugLevel=0;

	static ArrayList<String> debugCategories =new ArrayList<String>();

	public static void setDebugging(boolean d){
		debugging = d;
	}

	public static void addDebugCategory(String cls){
		debugCategories.add(cls);
	}

	public static void clearDebugCategories(){
		debugCategories.clear();
	}

	public static boolean isDebugging(){
		return debugging;
	}

	public static void logDebugMessage(boolean condition, LogBuilder builder){
		if (isDebugging()){
			if (condition && debugCategories.contains(builder.getCategory())){
				System.out.println(builder.buildLog());
			}
		}
	}

	public static void test1(){
		MTTaskGraph g1 = new MTTaskGraph("g1");

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

		g1.addEdge(new MTTaskEdge(12,n1,n2));
		g1.addEdge(new MTTaskEdge(100,n2,n4));
		g1.addEdge(new MTTaskEdge(29,n2,n3));
		g1.addEdge(new MTTaskEdge(10,n3,n4));
		g1.addEdge(new MTTaskEdge(18,n3,n5));
		g1.addEdge(new MTTaskEdge(12,n4,n1));
		g1.addEdge(new MTTaskEdge(25,n4,n2));
		g1.addEdge(new MTTaskEdge(50,n5,n5));

		MTTaskGraph g2 = new MTTaskGraph("g2");
		MTTaskNode n21 = new MTTaskNode(7,15,"n21");
		MTTaskNode n22 = new MTTaskNode(5,12,"n22");
		g2.addNode(n21);
		g2.addNode(n22);

		g2.addEdge(new MTTaskEdge(15,n21,n22));
		g2.addEdge(new MTTaskEdge(12,n22,n21));

		MTDigraphScheduler sched = new MTDigraphScheduler();

		sched.addTask(g1);
		sched.addTask(g2);

		System.out.println("schedulable: "+sched.isTasksetSchedulable());

		TreeSet<MTTaskGraph> set = sched.getTaskset();

		for (MTTaskGraph g:set){
			System.out.println(g.getName()+": priority: "+g.getPriority());
		}
	}


	public static void testMTSample1(){

		MTTaskGraph g1 = new MTTaskGraph("g1");

		MTTaskNode gt1 = new MTTaskNode(1,2,"gt1");
		MTTaskNode k1 = new MTTaskNode(1,2,"k1");
		MTTaskNode kt1 = new MTTaskNode(1,4,"kt1");
		MTTaskNode gtp1 = new MTTaskNode(2,6,"gtp1");
		MTTaskNode kp1 = new MTTaskNode(1,4,"kp1");
		MTTaskNode ktp1 = new MTTaskNode(1,10,"ktp1");

		g1.addNode(gt1);
		g1.addNode(k1);;
		g1.addNode(kt1);
		g1.addNode(gtp1);
		g1.addNode(kp1);
		g1.addNode(ktp1);

		MTTaskEdge e1 = new MTTaskEdge(4,gt1,gt1);
		g1.addEdge(e1);
		MTTaskEdge e2 = new MTTaskEdge(2,gt1,k1);
		g1.addEdge(e2);
		MTTaskEdge e3 = new MTTaskEdge(2,k1,gt1);
		g1.addEdge(e3 );
		MTTaskEdge e4 = new MTTaskEdge(2,gt1,kt1);
		g1.addEdge(e4);
		MTTaskEdge e5 = new MTTaskEdge(4, kt1, kt1);
		g1.addEdge(e5);
		MTTaskEdge e6 = new MTTaskEdge(4, kt1, gtp1);
		g1.addEdge(e6);
		MTTaskEdge e7 = new MTTaskEdge(10, gtp1, gtp1);
		g1.addEdge(e7);
		MTTaskEdge e8 = new MTTaskEdge(6,gtp1,kp1);
		g1.addEdge(e8);
		MTTaskEdge e9 =  new MTTaskEdge(4,kp1,gtp1);
		g1.addEdge(e9);
		MTTaskEdge e10 = new MTTaskEdge(6, gtp1, ktp1);
		g1.addEdge(e10);
		MTTaskEdge e30 = new MTTaskEdge(10, ktp1,ktp1);
		g1.addEdge(e30);
		MTTaskEdge e11 = new MTTaskEdge(10,ktp1,gt1);
		g1.addEdge(e11);

		MTTaskGraph g2 = new MTTaskGraph("g2");

		MTTaskNode gt2 = new MTTaskNode(2,5,"gt2");
		MTTaskNode k2 = new MTTaskNode(1,3, "k2");
		MTTaskNode kt2 = new MTTaskNode(1,3, "kt2");
		MTTaskNode gtp2 = new MTTaskNode(2, 10, "gtp2");
		MTTaskNode kp2 = new MTTaskNode(1, 5, "kp2");
		MTTaskNode ktp2 = new MTTaskNode(1, 5, "ktp2");

		g2.addNode(gt2);
		g2.addNode(k2);
		g2.addNode(kt2);
		g2.addNode(gtp2);
		g2.addNode(kp2);
		g2.addNode(ktp2);

		MTTaskEdge e12 = new MTTaskEdge(8,gt2,gt2);
		g2.addEdge(e12);
		MTTaskEdge e13 = new MTTaskEdge(5,gt2,k2);
		g2.addEdge(e13);
		MTTaskEdge e14 =  new MTTaskEdge(3,k2,gt2);
		g2.addEdge(e14);
		MTTaskEdge e15 = new MTTaskEdge(3,gt2,kt2);
		g2.addEdge(e15);
		MTTaskEdge e16 = new MTTaskEdge(8, kt2, kt2);
		g2.addEdge(e16);
		MTTaskEdge e17 = new MTTaskEdge(3, kt2, gtp2);
		g2.addEdge(e17);
		MTTaskEdge e18 = new MTTaskEdge(15, gtp2, gtp2);
		g2.addEdge(e18);
		MTTaskEdge e19 = new MTTaskEdge(10,gtp2,kp2);
		g2.addEdge(e19 );
		MTTaskEdge e20 = new MTTaskEdge(5,kp2,gtp2);
		g2.addEdge(e20);
		MTTaskEdge e21 =  new MTTaskEdge(10, gtp2, ktp2);
		g2.addEdge(e21);
		MTTaskEdge e31 = new MTTaskEdge(5,ktp2,ktp2);
		g2.addEdge(e31);
		MTTaskEdge e22 = new MTTaskEdge(5,ktp2,gt2);
		g2.addEdge(e22);

		MTDigraphScheduler sched = new MTDigraphScheduler();

		sched.addTask(g1);
		sched.addTask(g2);

		System.out.println("Schedulable: "+sched.isTasksetSchedulable());
		TreeSet<MTTaskGraph> set = sched.getTaskset();

		for (MTTaskGraph g:set){
			System.out.println(g.getName()+": priority: "+g.getPriority());
		}
	}

	public static void testRTJ2015Fig14(){
		MTTaskGraph g1 = new MTTaskGraph();
		MTTaskGraph g2 = new MTTaskGraph();
		MTTaskGraph g3 = new MTTaskGraph();

		MTTaskNode n1 = new MTTaskNode(2,5,"n1");
		n1.setPriority(1);
		n1.setPreemptible(false);
		MTTaskEdge e1 = new MTTaskEdge(5,n1,n1);
		g1.addNode(n1);
		g1.addEdge(e1);

		MTTaskNode n2 = new MTTaskNode(2,7,"n2");
		n2.setPriority(2);
		n2.setPreemptible(false);
		MTTaskEdge e2 = new MTTaskEdge(7,n2,n2);
		g2.addNode(n2);
		g2.addEdge(e2);

		MTTaskNode n3 = new MTTaskNode(2,6,"n3");
		n3.setPriority(3);
		n3.setPreemptible(false);
		MTTaskEdge e3 = new MTTaskEdge(7,n3,n3);
		g3.addNode(n3);
		g3.addEdge(e3);

		MTDigraphScheduler sched = new MTDigraphScheduler();

		sched.addTask(g1);
		sched.addTask(g2);
		sched.addTask(g3);

		System.out.println("sched: "+sched.isTasksetMTSchedulable());
	}

	public static void schedTest1(){
		var gen = new MTDigraphTaskGenerator();


		var system = gen.createModalSystem(0.6, 2,0,
				2,1,100,1000,0.1);

		var set = new ArrayList<MTTaskGraph>();

		for (var m:system.getModes()){
			set.addAll(m.getTaskset());
		}

		for (var t:system.getSystemTransitions()){
			set.addAll(t.getTaskTransitions());
		}

		var sched = new MTDigraphScheduler();

		MTDigraphScheduler.setDebugging(false);

		System.out.println("schedulable: "+sched.isMTModalSystemSchedulable(system));


		// activate all modes
		for (MTDigraphMode m:system.getModes()){
			m.setActive(true);
		}
		// and all transitions
		for (MTDigraphModeTransition t:system.getSystemTransitions()){
			t.setActive(true);
		}

		//MTDigraphVisualizer v = new MTDigraphVisualizer(set);
		//v.show();

	}

	public static void increasingUtilization(FileWriter writer, FileWriter timeWriter) throws IOException {
		MTDigraphScheduler.setDebugging(false);

		int numExperiments=1000;
		int successCount;
		int mtsuccessCount;
		var gen = new MTDigraphTaskGenerator();

		writer.write("Utilization,Schedulable, MT Schedulable\n");
		for (double util = 0.3 ; util <= 1.0 ; util += 0.1) {
			successCount=0;
			mtsuccessCount=0;
			long millisTotal=0;
			long start,end;
			for (int i=0;i<numExperiments;i++) {
				var system = gen.createModalSystem(util, 10, 0,
						2, 1, 100, 1000, 0.1);
				var sched = new MTDigraphScheduler();
				var mtsystem = MTTaskFromDigraphGenerator.GenerateMTTasksetFromDigraph(system);
				var mtsched = new LayeredTrustExactScheduler();
				mtsched.add(mtsystem);
				mtsched.assignDeadlineMonotonicPriorities();
				boolean mtschedulable = mtsched.isSchedulable();
				mtsuccessCount += mtschedulable ? 1 : 0 ;

				start = System.currentTimeMillis();
				successCount += sched.isMTModalSystemSchedulable(system) ? 1 : 0;
				end = System.currentTimeMillis();
				millisTotal += (end-start);
			}
			System.out.print("Increasing Utilization: utilization:"+util+"\r");
			writer.write(util+"," + ((double)successCount)/((double) numExperiments)+","
					+ ((double)mtsuccessCount)/((double) numExperiments)+"\n");
			millisTotal = millisTotal / numExperiments;
			timeWriter.write(util+","+millisTotal+"\n");
		}
		System.out.println("");
	}

	public static void increasingNumTasks(FileWriter writer, FileWriter timeWriter) throws IOException {
		MTDigraphScheduler.setDebugging(false);

		int numExperiments=1000;
		int successCount;
		var gen = new MTDigraphTaskGenerator();

		writer.write("Tasks per Mode,Schedulable\n");
		for (int taskPerMode = 2 ; taskPerMode<= 10 ; taskPerMode++) {
			successCount=0;
			long millisTotal=0;
			long start,end;

			for (int i=0;i<numExperiments;i++) {
				var system = gen.createModalSystem(0.7, taskPerMode, 0,
						2, 1, 100, 1000, 0.1);
				var sched = new MTDigraphScheduler();
				start = System.currentTimeMillis();
				successCount += sched.isMTModalSystemSchedulable(system) ? 1 : 0;
				end = System.currentTimeMillis();
				millisTotal += (end-start);
			}
			System.out.print("Increasing Task Per Mode: tasks:"+taskPerMode+"\r");
			writer.write(taskPerMode+"," + ((double)successCount)/((double) numExperiments)+"\n");
			millisTotal = millisTotal / numExperiments;
			timeWriter.write(taskPerMode+","+millisTotal+"\n");
		}
		System.out.println("");
	}

	public static void increasingDegradationDepth(FileWriter writer,FileWriter timeWriter) throws IOException {
		MTDigraphScheduler.setDebugging(false);
		//MTDigraphScheduler.addDebugCategory("sched");
		MTDigraphScheduler.addDebugCategory("sched");


		int numExperiments=1000;
		int successCount;
		int mtsuccessCount;
		var gen = new MTDigraphTaskGenerator();

		writer.write("Degradation Depth,Schedulable\n");
		for (int depth = 2 ; depth <=10 ; depth++) {
			successCount=0;
			mtsuccessCount=0;
			long millisTotal=0;
			long start,end;

			for (int i=0;i<numExperiments;i++) {
				var system = gen.createModalSystem(0.7, 10, 0,
						depth, 1, 100, 1000, 0.1);
				var sched = new MTDigraphScheduler();
				start = System.currentTimeMillis();

				var mtsystem = MTTaskFromDigraphGenerator.GenerateMTTasksetFromDigraph(system);
				var mtsched = new LayeredTrustExactScheduler();
				mtsched.add(mtsystem);
				mtsched.assignDeadlineMonotonicPriorities();
				boolean mtschedulable = mtsched.isSchedulable();
				mtsuccessCount += mtschedulable ? 1 : 0 ;

				boolean schedulable= sched.isMTModalSystemSchedulable(system);
				end = System.currentTimeMillis();
				millisTotal += (end-start);
				successCount += schedulable ? 1 : 0;
				MTDigraphScheduler.logDebugMessage(true,
						new LogBuilder(new Object[]{system,schedulable},"density") {
							@Override
							public String buildLog() {
								String str="";
								MTDigraphModalSystem s = (MTDigraphModalSystem)parms[0];
								str += (Boolean)parms[1] ? "Schedulable: " : "\t\t\t\t\tNOT Schedulable ";
								str += "Max Density["+s.getMaxDensity()+"]";
								return str;
							}
						});
			}
			System.out.print("Increasing Degradation Depth: depth:"+depth+"\r");
			writer.write(depth+"," + ((double)successCount)/((double) numExperiments)+","+((double)mtsuccessCount)/((double) numExperiments)+"\n");
			millisTotal = millisTotal / numExperiments;
			timeWriter.write(depth+","+millisTotal+"\n");
		}
		System.out.println("\nIncreasing Degradation Depth: DONE");
	}

	public static void increasingPeriodRatio(FileWriter writer,FileWriter timeWriter) throws IOException {
		MTDigraphScheduler.setDebugging(false);

		int numExperiments=1000;
		int successCount;
		var gen = new MTDigraphTaskGenerator();
		long minPeriod=100;
		long maxPeriod=0;

		writer.write("Period Ratio,Schedulable\n");
		for (long ratio = 2 ; ratio <= 64; ratio = (ratio * 2)) {
			successCount=0;
			long millisTotal=0;
			long start,end;

			maxPeriod = minPeriod * ratio;
			for (int i=0;i<numExperiments;i++) {
				var system = gen.createModalSystem(0.7, 10, 0,
						2, 1, minPeriod, maxPeriod, 0.1);
				var sched = new MTDigraphScheduler();
				start = System.currentTimeMillis();
				successCount += sched.isMTModalSystemSchedulable(system) ? 1 : 0;
				end = System.currentTimeMillis();
				millisTotal += (end-start);
			}
			writer.write(ratio+"," + ((double)successCount)/((double) numExperiments)+"\n");
			System.out.print("Increasing Ratio: ratio: "+ratio+"\r");
			millisTotal = millisTotal / numExperiments;
			timeWriter.write(ratio+","+millisTotal+"\n");
		}
		System.out.println("");
	}

	public static void increasingHTExecution(FileWriter writer,FileWriter timeWriter) throws IOException {
		MTDigraphScheduler.setDebugging(false);

		int numExperiments=1000;
		int successCount;
		var gen = new MTDigraphTaskGenerator();
		long minPeriod=100;
		long maxPeriod=0;

		maxPeriod = minPeriod * 16;

		writer.write("HT Util Percentage,Schedulable\n");
		for (double htUtilPortion = 0.1;htUtilPortion <= 0.5 ; htUtilPortion += 0.05) {
			successCount=0;
			long millisTotal=0;
			long start,end;

			for (int i=0;i<numExperiments;i++) {
				var system = gen.createModalSystem(0.7, 10, 0,
						2, 1, minPeriod, maxPeriod, htUtilPortion);
				var sched = new MTDigraphScheduler();
				start = System.currentTimeMillis();
				successCount += sched.isMTModalSystemSchedulable(system) ? 1 : 0;
				end = System.currentTimeMillis();
				millisTotal += (end-start);
			}
			writer.write(htUtilPortion+"," + ((double)successCount)/((double) numExperiments)+"\n");
			System.out.print("Increasing HT Util Percentage: percentage: "+htUtilPortion+"\r");
			millisTotal = millisTotal / numExperiments;
			timeWriter.write(htUtilPortion+","+millisTotal+"\n");
		}
		System.out.println("");
	}


	public static void main(String [] args){
		//dtest1();
		//testMTSample1();
		//testRTJ2015Fig14();
		schedTest1();;
//		FileWriter writer=null;
//		FileWriter timeWriter=null;
//		try {
//			writer = new FileWriter("increasingUtilization.csv");
//			timeWriter = new FileWriter("increasingUtilizationTimes.csv");
//			increasingUtilization(writer,timeWriter);
//			writer.close();
//			timeWriter.close();
//			writer = new FileWriter("increasingTasksPerMode.csv");
//			timeWriter = new FileWriter("increasingTasksPerModeTimes.csv");
//			increasingNumTasks(writer,timeWriter);
//			writer.close();
//			timeWriter.close();
//			writer = new FileWriter("increasingDegradationDepth.csv");
//			timeWriter = new FileWriter("increasingDegradationDepthTimes.csv");
//			increasingDegradationDepth(writer,timeWriter);
//			writer.close();
//			timeWriter.close();
//			writer = new FileWriter("increasingPeriodRatio.csv");
//			timeWriter = new FileWriter("increasingPeriodRatioTimes.csv");
//			increasingPeriodRatio(writer,timeWriter);
//			writer.close();
//			timeWriter.close();
//			writer = new FileWriter("IncreasingHTUtil.csv");
//			timeWriter = new FileWriter("increasingHTUtilTimes.csv");
//			increasingHTExecution(writer,timeWriter);
//			writer.close();
//			timeWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
