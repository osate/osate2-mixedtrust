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
