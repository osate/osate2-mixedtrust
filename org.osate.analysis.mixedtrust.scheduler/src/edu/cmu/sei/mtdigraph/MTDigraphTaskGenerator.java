package edu.cmu.sei.mtdigraph;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class MTDigraphTaskGenerator {

    long nextTaskNum=1;
    /**
     * This create a mixed-trust task digraph
     * @param T
     * @param C
     * @param kC
     * @param E
     * @param D
     * @return
     */
    public MTTaskGraph createMixedTrustGraphTask(long T, long C, long kC, long E, long D){
        MTTaskGraph g = new MTTaskGraph();
        MTTaskNode gt = new MTTaskNode(true , 1, C, D, "GT"+Long.toString(nextTaskNum));
        MTTaskNode ht = new MTTaskNode(false , 1, kC, D, "HT"+Long.toString(nextTaskNum));
        nextTaskNum++;
        MTTaskEdge eT = new MTTaskEdge(T,gt,gt);
        // we initialize the
        MTTaskEdge eE = new MTTaskEdge(E, gt,ht);
        MTTaskEdge eTE = new MTTaskEdge((T-E),ht,gt);

        gt.setMtPartnerEdge(eE);
        ht.setMtPartnerEdge(eTE);

        g.addNode(gt);
        g.addNode(ht);
        g.addEdge(eT);
        g.addEdge(eE);
        g.addEdge(eTE);

        return g;
    }

    /**
     * This will create the transitioning graph that includes the mixed-trust GT and HT nodes and the transition
     * from HT to GT to capture the last potential execution after the mode transition event.
     * This should be called only after the corresponding source and target mixed-trust task graphs had already been
     * created so that the MTTargetEdge is properly initialized.
     *
     * @param fromGT
     * @param toGT
     * @param kC
     * @param srcEdgeInterarrival
     * @param tgtEdgeInterarrival
     * @param T
     */
    public MTTaskGraph createModeTransitionGraphHTask(MTTaskNode fromGT, MTTaskNode toGT, long kC, long srcEdgeInterarrival,
                                     long tgtEdgeInterarrival, long T){
        MTTaskNode ht = new MTTaskNode(false , 1, kC, tgtEdgeInterarrival, "HTt"+nextTaskNum++);
        ht.setTransitioningHT(true);
        MTTaskEdge srcE = new MTTaskEdge(srcEdgeInterarrival,fromGT,ht);
        MTTaskEdge tgtE = new MTTaskEdge(tgtEdgeInterarrival,ht,toGT);
        MTTaskEdge tE = new MTTaskEdge(T,ht,ht);

        MTTaskGraph tg = new MTTaskGraph();

        // The transitioning HT should be added first by convention.
        tg.addNode(ht);
        tg.addNode(fromGT);
        tg.addNode(fromGT.getMtPartnerEdge().getTargetNode());
        tg.addNode(toGT);
        tg.addNode(toGT.getMtPartnerEdge().getTargetNode());
        // add edge from srcHT to srcGT
        tg.addEdge(fromGT.getMtPartnerEdge().getTargetNode().getMtPartnerEdge());
        // add edge from tgtHT to tgtGT
        tg.addEdge(toGT.getMtPartnerEdge().getTargetNode().getMtPartnerEdge());
        // add edge from tgtGT to tgtHT
        tg.addEdge(toGT.getMtPartnerEdge());
        tg.addEdge(srcE);
        tg.addEdge(tgtE);
        tg.addEdge(tE);
        return tg;
    }

    Random rand = new Random();

    public MTDigraphMode createMode(double utilization, long numTaskPerMode, long minPeriod, long maxPeriod, double deadlinePortion, double htUtilPortion){
        MTDigraphMode m = new MTDigraphMode();

        double guestUtil = 0.0;
        double hyperUtil =0.0;
        double utilPerTask = utilization / numTaskPerMode;
        for (int i=0;i<numTaskPerMode;i++){
            long T= minPeriod + (long) (rand.nextDouble() *(maxPeriod-minPeriod));
            long C = (long)(utilPerTask * T) ;
            long kC = (long) (C * htUtilPortion);
            C = (long) (C * (1-htUtilPortion));
            // E will be adjusted after scheduling HTs
            long D = (long)(T*deadlinePortion);
            long E = T/2;
            m.addTask(createMixedTrustGraphTask(T,C,kC,E,D));
            guestUtil += ((double)C)/((double)T);
            hyperUtil += ((double)kC)/((double)T);
        }

        MTDigraphScheduler.logDebugMessage(true,
                new LogBuilder(new Object[]{guestUtil,hyperUtil},"util") {
                    @Override
					public String buildLog() {
                        String str="";
                        str += "Mode Guest-Util["+ parms[0] +"], Hyper-Util["+parms[1]+"]";
                        return str;
                    }
                });

        return m;
    }

    public MTDigraphModeTransition createDegradingTransition(long numTasksPerMode,long numPersistentTasks, MTDigraphMode parentMode, MTDigraphMode childMode){
        MTDigraphModeTransition transition = new MTDigraphModeTransition(parentMode,childMode);

        long numDegradingTasks = numTasksPerMode - numPersistentTasks;


        int pIdx=0;
        int dIdx=0;
        for (int k=0;k<numDegradingTasks;k++){
            // select pairs
            MTTaskGraph srcG = parentMode.getTaskset().get(pIdx++);
            MTTaskNode srcGT = srcG.getNodes().get(0);
            if (!srcGT.isPreemptible()) {
				srcGT = srcGT.getMtPartnerEdge().getTargetNode();
			}
            MTTaskGraph tgtG = childMode.getTaskset().get(dIdx++);
            MTTaskNode tgtGT = tgtG.getNodes().get(0);
            if (!tgtGT.isPreemptible()) {
				tgtGT = tgtGT.getMtPartnerEdge().getTargetNode();
			}
			@SuppressWarnings("unused")
			long srcEdgeInterarrival = 0, tgtEdgeInterarrival = 0, T = 0, invT = 0;
            for (MTTaskEdge e:srcG.nodeToSuccessorEdges.get(srcGT)){
                // transition from GT to HT
                if (e.getSourceNode() != e.getTargetNode()){
                    srcEdgeInterarrival = e.getInterarrival();
                } else {
                    // self loop period
                    T = e.getInterarrival();
                }
            }
            for (MTTaskEdge e:tgtG.nodeToSuccessorEdges.get(tgtGT)) {
                // transition from GT to HT
                if (e.getSourceNode() != e.getTargetNode()){
                    tgtEdgeInterarrival = e.getInterarrival();
                } else {
                    invT = e.getInterarrival();
                }
            }
            MTTaskGraph tg = createModeTransitionGraphHTask(srcGT , tgtGT, srcGT.getMtPartnerEdge().getTargetNode().getWCET(), srcEdgeInterarrival,
                    srcEdgeInterarrival, T);
            transition.addTaskTransition(tg);
        }
        return transition;
    }

    public void createModalSystemLayer(double utilization, long numTasksPerMode, long numPersistentTasks, long degradationDepth,
                                       long currentDepth,
                                       long childrenPerMode, long minPeriod, long maxPeriod, double htUtilPortion,
                                       MTDigraphModalSystem system, MTDigraphMode parentMode){

        for (long j=0;j<childrenPerMode;j++){
            MTDigraphMode m2 = createMode(utilization,numTasksPerMode,minPeriod,maxPeriod,1.0,.1);
            system.addMode(m2);
            MTDigraphModeTransition transition1 = createDegradingTransition(numTasksPerMode,numPersistentTasks,parentMode,m2);
            system.addSystemTransition(transition1);
            currentDepth++;
            if (currentDepth < degradationDepth) {
                createModalSystemLayer(utilization, numTasksPerMode, numPersistentTasks, degradationDepth, currentDepth,
                        childrenPerMode, minPeriod, maxPeriod, htUtilPortion,
                        system, m2);
            }
        }
    }

    public void createModalSystemLayer2(double utilization, long numTasksPerMode, long numPersistentTasks, long degradationDepth,
                                       long currentDepth,
                                       long childrenPerMode, long minPeriod, long maxPeriod, double htUtilPortion,
                                       MTDigraphModalSystem system, MTDigraphMode parentMode) {
        for (long j = 0; j < childrenPerMode; j++) {
            long degradingTasks = numTasksPerMode - numPersistentTasks;
            MTDigraphMode childMode = new MTDigraphMode();
            var transition = new MTDigraphModeTransition(parentMode, childMode);

            double guestUtil=0.0;
            double hyperUtil=0.0;
            double transUtil=0.0;
            for (var g : parentMode.getTaskset()) {
                if (degradingTasks <= 0) {
					break;
				}
                degradingTasks--;
                MTTaskNode gt = g.getNodes().get(0);
                long T = (gt.isPreemptible() ? gt.getMtPartnerEdge().getInterarrival() : gt.getMtPartnerEdge().getTargetNode().getMtPartnerEdge().getInterarrival());
                for (MTTaskEdge e : g.getSuccessorEdges(gt)) {
                    if (e.getSourceNode() == e.getTargetNode()) {
                        T = e.getInterarrival();
                        break;
                    }
                }
                long D = (gt.isPreemptible() ? gt.getDeadline() : gt.getMtPartnerEdge().getTargetNode().getDeadline());
                long C = (gt.isPreemptible() ? gt.getWCET() : gt.getMtPartnerEdge().getTargetNode().getWCET());
                long kC = (gt.isPreemptible() ? gt.getMtPartnerEdge().getTargetNode().getWCET() : gt.getWCET());
                long E = (gt.isPreemptible() ? gt.getMtPartnerEdge().getInterarrival() : gt.getMtPartnerEdge().getTargetNode().getResponseTime());
                // enlarge T randomly
                long eT = (long) (rand.nextDouble() * (maxPeriod - T));
                long newC = (long)((double)C * ((double)(T+eT))/(T));
                long newkC= (long)((double)kC * ((double)(T+eT))/(T));
                long newT = T + eT;
                long newD = D + eT;

                guestUtil += ((double)newC)/((double)newT);
                hyperUtil += ((double)newkC)/((double)newT);
                transUtil += ((double)newkC)/((double)T);

                MTTaskGraph g1 = createMixedTrustGraphTask(newT, C, kC, E, newD);

                childMode.addTask(g1);
                MTTaskNode gt1 = g1.getNodes().get(0);
                if (!gt1.isPreemptible()) {
					gt1 = gt1.getMtPartnerEdge().getTargetNode();
				}
                MTTaskGraph t1 = createModeTransitionGraphHTask(gt, gt1, kC, E, E, T);
                transition.addTaskTransition(t1);
            }
            system.addMode(childMode);
            system.addSystemTransition(transition);

            MTDigraphScheduler.logDebugMessage(true,
                    new LogBuilder(new Object[]{guestUtil,hyperUtil,transUtil},"util") {
                        @Override
						public String buildLog() {
                            String str="";
                            str += "Next Mode Guest-Util["+ parms[0] +"], Hyper-Util["+parms[1]+"],"+
                                    " Trans-Util["+parms [2]+"]";
                            return str;
                        }
                    });

            currentDepth++;
            if (currentDepth < degradationDepth) {
                createModalSystemLayer2(utilization, numTasksPerMode, numPersistentTasks, degradationDepth, currentDepth,
                        childrenPerMode, minPeriod, maxPeriod, htUtilPortion,
                        system, childMode);
            }
        }
    }


    public MTDigraphModalSystem createModalSystem(double utilization, long numTasksPerMode, long numPersistentTasks, long degradationDepth, long childrenPerMode, long minPeriod, long maxPeriod, double htUtilPortion){
        MTDigraphModalSystem system = new MTDigraphModalSystem();

        // create the root mode
        MTDigraphMode rootMode = createMode(utilization,numTasksPerMode,minPeriod,maxPeriod,1.0,htUtilPortion);
        system.addMode(rootMode);

        MTDigraphMode parentMode = rootMode;



        if (degradationDepth>1) {
            createModalSystemLayer2(utilization, numTasksPerMode, numPersistentTasks, degradationDepth, 1,
                    childrenPerMode, minPeriod, maxPeriod, htUtilPortion,
                    system, parentMode);
        }

        var nodes = new TreeSet<MTTaskNode>(new IncreasingDeadlineComparator());
        for (var m:system.getModes()){
            for (var ts:m.getTaskset()) {
                nodes.addAll(ts.getNodes());
            }
        }
        for (var t:system.getSystemTransitions()){
            for (var g:t.getTaskTransitions()){
                nodes.addAll(g.getNodes());
            }
        }

        MTDigraphScheduler.assignDeadlineMonotonicPriorities(nodes);

        return system;
    }

    public static void test1(){
        var gen = new MTDigraphTaskGenerator();

        var system = gen.createModalSystem(0.5, 2,0,
                2,2,100,1000,0.1);

        var set = new ArrayList<MTTaskGraph>();

        for (var m:system.getModes()){
            set.addAll(m.getTaskset());
        }

        for (var t:system.getSystemTransitions()){
            set.addAll(t.getTaskTransitions());
        }
    }

    public static void main(String args[]){
        test1();
    }
}
