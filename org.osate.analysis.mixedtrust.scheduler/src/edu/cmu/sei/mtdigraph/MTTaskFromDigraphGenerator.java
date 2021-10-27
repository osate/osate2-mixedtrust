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

import java.util.ArrayList;

import edu.cmu.sei.mtzsrm.MixedTrustTask;

public class MTTaskFromDigraphGenerator {

    public static ArrayList<MixedTrustTask> GenerateMTTasksetFromDigraph(MTDigraphModalSystem digrahSystem){
        ArrayList<MixedTrustTask>mixedTrustTasks = new ArrayList<MixedTrustTask>();
        ArrayList<MTTaskGraph> processed = new ArrayList<MTTaskGraph>();
        for (var m:digrahSystem.getModes()){
            for (var g:m.getTaskset()){
                long maxGC=0;
                long maxHC=0;
                long minD = Long.MAX_VALUE;
                long minT = Long.MAX_VALUE;
                if (!processed.contains(g)){
                    processed.add(g);
                    for (var n:g.getNodes()){
                        if (n.isPreemptible()){
                            //GuestTask
                            if (n.getWCET()> maxGC){
                                maxGC = n.getWCET();
                            }
                            if (n.getDeadline() < minD){
                                minD = n.getDeadline();
                            }
                        } else {
                            // HyperTask
                            if (n.getWCET() > maxHC){
                                maxHC = n.getWCET();
                            }
                        }
                        for (var e:g.getSuccessorEdges(n)){
                            if (e.getTargetNode().equals(n)){
                                if (e.getInterarrival() < minT){
                                    minT = e.getInterarrival();
                                }
                            }
                        }

                    }
                    MixedTrustTask mttask = new MixedTrustTask(
                            (int) minT, // period
                            (int) minD, // deadline
                            0, // guest criticality
                            new int[] {(int)maxGC}, // guest exectimes
                            1, // hyper criticality
                            (int) maxHC, // hyper exectime
                            0);   // priority

                    mixedTrustTasks.add(mttask);
                }
            }
        }

        return mixedTrustTasks;
    }
}
