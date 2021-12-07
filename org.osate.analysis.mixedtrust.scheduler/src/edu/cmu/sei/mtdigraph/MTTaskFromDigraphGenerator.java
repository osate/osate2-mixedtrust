package edu.cmu.sei.mtdigraph;

import edu.cmu.sei.mtzsrm.MixedTrustTask;

import java.util.ArrayList;

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
