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

public class MTDigraphModalSystem implements Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = 7520659259329720842L;

	ArrayList<MTDigraphMode> modes = new ArrayList<MTDigraphMode>();

    ArrayList<MTDigraphModeTransition> systemTransitions = new ArrayList<MTDigraphModeTransition>();

    public void addMode(MTDigraphMode m){
        modes.add(m);
    }

    public void addSystemTransition(MTDigraphModeTransition t){
        systemTransitions.add(t);
    }

    public ArrayList<MTDigraphMode> getModes(){
        return modes;
    }

    public ArrayList<MTDigraphModeTransition> getSystemTransitions(){
        return systemTransitions;
    }

    public double getUtilization(){
        return 0.0;
    }

    public double getMaxDensity(){
        double maxD=0.0;
        for (MTDigraphMode m:modes) {
            double maxDmode=0.0;
            for (MTTaskGraph g : m.getTaskset()) {
                double maxDtask=0.0;
                for (MTTaskNode n : g.getNodes()) {
                    double d = ((double) n.getWCET()) / ((double) n.getDeadline());
                    if (d > maxDtask) {
                        maxDtask = d;
                    }
                }
                maxDmode += maxDtask;
            }
            if (maxDmode>maxD){
                maxD = maxDmode;
            }
        }

        for (var t:getSystemTransitions()){
            double maxDtrans=0.0;
            for (var tt:t.getTaskTransitions()){
                double maxDtask = 0.0;
                for (var n:tt.getNodes()){
                    double d = ((double) n.getWCET()) / ((double) n.getDeadline());
                    if (d > maxDtask) {
                        maxDtask = d;
                    }
                }
                maxDtrans += maxDtask;
            }
            if (maxD<maxDtrans){
                maxD = maxDtrans;
            }
        }
        return maxD;
    }

    public static void buildSampleModalSystem(){
        MTTaskGraph g1 = new MTTaskGraph();
        MTTaskGraph g1p = new MTTaskGraph();

        MTTaskNode k1 = new MTTaskNode(false, 1,2,10,"k1");
        MTTaskNode t1 = new MTTaskNode(true, 10,5,20,"t1");
        MTTaskEdge et1 = new MTTaskEdge(20,t1,t1);
        MTTaskEdge eK1T1 = new MTTaskEdge(10,k1,t1);
        MTTaskEdge eT1K1 = new MTTaskEdge(10,t1,k1);

        g1.addNode(k1);
        g1.addNode(t1);
        g1.addEdge(et1);
        g1.addEdge(eK1T1);
        g1.addEdge(eT1K1);

        MTTaskNode k1p = new MTTaskNode(false,1,2,10,"k1p");
        MTTaskNode t1p = new MTTaskNode(true, 10,5,20,"t1p");
        MTTaskEdge et1p = new MTTaskEdge(20,t1p,t1p);
        MTTaskEdge eK1pT1p = new MTTaskEdge(10,k1p,t1p);
        MTTaskEdge eT1pK1p = new MTTaskEdge(10, t1p,k1p);

        g1p.addNode(k1p);
        g1p.addNode(t1p);
        g1p.addEdge(et1p);
        g1p.addEdge(eK1pT1p);
        g1p.addEdge(eT1pK1p);

        MTTaskGraph g1t = new MTTaskGraph();

        MTTaskNode k1t = new MTTaskNode(false,1,2,10,"k1t");
        MTTaskEdge et1k1t = new MTTaskEdge(10,t1,k1t);
        MTTaskEdge ek1tk1t = new MTTaskEdge(20,k1t,k1t);
        MTTaskEdge ek1tt1p = new MTTaskEdge(10,k1t,t1p);

        g1t.addNode(k1);
        g1t.addNode(t1);
        g1t.addNode(k1t);
        g1t.addNode(t1p);
        g1t.addNode(k1p);
        g1t.addEdge(eK1T1);
        g1t.addEdge(et1p);
        g1t.addEdge(eK1pT1p);
        g1t.addEdge(eT1pK1p);
        g1t.addEdge(et1k1t);
        g1t.addEdge(ek1tk1t);
        g1t.addEdge(ek1tt1p);

        MTTaskGraph g2 = new MTTaskGraph();
        MTTaskGraph g2p = new MTTaskGraph();

        MTTaskNode k2 = new MTTaskNode(false, 1,2,10,"k2");
        MTTaskNode t2 = new MTTaskNode(true, 10,5,20,"t2");
        MTTaskEdge et2 = new MTTaskEdge(20,t2,t2);
        MTTaskEdge eK2T2 = new MTTaskEdge(10,k2,t2);
        MTTaskEdge eT2K2 = new MTTaskEdge(10,t2,k2);

        g2.addNode(k2);
        g2.addNode(t2);
        g2.addEdge(et2);
        g2.addEdge(eK2T2);
        g2.addEdge(eT2K2);

        MTTaskNode k2p = new MTTaskNode(false,1,2,10,"k2p");
        MTTaskNode t2p = new MTTaskNode(true, 10,5,20,"t2p");
        MTTaskEdge et2p = new MTTaskEdge(20,t2p,t2p);
        MTTaskEdge eK2pT2p = new MTTaskEdge(10,k2p,t2p);
        MTTaskEdge eT2pK2p = new MTTaskEdge(10, t2p,k2p);

        g2p.addNode(k2p);
        g2p.addNode(t2p);
        g2p.addEdge(et2p);
        g2p.addEdge(eK2pT2p);
        g2p.addEdge(eT2pK2p);




        MTTaskGraph g2t = new MTTaskGraph();

        MTTaskNode k2t = new MTTaskNode(false,1,2,10,"k2t");
        MTTaskEdge et2k2t = new MTTaskEdge(10,t2,k2t);
        MTTaskEdge ek2tk2t = new MTTaskEdge(20,k2t,k2t);
        MTTaskEdge ek2tt2p = new MTTaskEdge(10,k2t,t2p);

        g2t.addNode(k2);
        g2t.addNode(t2);
        g2t.addNode(k2t);
        g2t.addNode(t2p);
        g2t.addNode(k2p);
        g2t.addEdge(eK2T2);
        g2t.addEdge(et2p);
        g2t.addEdge(eK2pT2p);
        g2t.addEdge(eT2pK2p);
        g2t.addEdge(et2k2t);
        g2t.addEdge(ek2tk2t);
        g2t.addEdge(ek2tt2p);

        MTDigraphMode mode1 = new MTDigraphMode();

        mode1.addTask(g1);
        mode1.addTask(g2);

        MTDigraphMode mode2 = new MTDigraphMode();

        mode2.addTask(g1p);
        mode2.addTask(g2p);

        MTDigraphModeTransition transition1 = new MTDigraphModeTransition(mode1,mode2);

        transition1.addTaskTransition(g1t);
        transition1.addTaskTransition(g2t);

        MTDigraphModalSystem system = new MTDigraphModalSystem();

        system.addMode(mode1);
        system.addMode(mode2);
        system.addSystemTransition(transition1);

        var set = new ArrayList<MTTaskGraph>();

        set.add(g1);
        set.add(g2);
        set.add(g1p);
        set.add(g2p);
        set.add(g1t);
        set.add(g2t);

        mode1.setActive(true);
        transition1.setActive(true);
        mode2.setActive(true);

        var mttaskset = MTTaskFromDigraphGenerator.GenerateMTTasksetFromDigraph(system);

        for (var mttask:mttaskset){
            System.out.println(mttask);
        }
    }

    public static void main(String[] args){
        buildSampleModalSystem();
    }
}
