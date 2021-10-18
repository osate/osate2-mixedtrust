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
package edu.cmu.sei.mtzsrm;

import java.util.Comparator;
import java.util.TreeSet;

public class DecreasingGuestCriticalityPriorityComparator implements Comparator<MixedTrustTask> {

	public int compare(MixedTrustTask o1, MixedTrustTask o2) {
		if (o1.getUniqueId() == o2.getUniqueId())
			return 0;
		if (o2.getGuestTask().getCritcality() == o1.getGuestTask().getCritcality()){
			if (o1.getPriority() == o2.getPriority())
				return (int) (o1.getUniqueId() - o2.getUniqueId());
			else
				return o2.getPriority() -o1.getPriority();
		} else
			return o2.getGuestTask().getCritcality() - o1.getGuestTask().getCritcality();
	}

	public static void main(String args[]){
		MixedTrustTask mt1 = new MixedTrustTask(
				100, // period
				100, // deadline
				1, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				3);   // hyper priority
		MixedTrustTask mt2 = new MixedTrustTask(
				100, // period
				100, // deadline
				2, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				3);   // hyper priority
		MixedTrustTask mt3 = new MixedTrustTask(
				100, // period
				100, // deadline
				3, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				3);   // hyper priority
		MixedTrustTask mt4 = new MixedTrustTask(
				100, // period
				100, // deadline
				3, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				2);   // hyper priority
		MixedTrustTask mt5 = new MixedTrustTask(
				100, // period
				100, // deadline
				3, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				1);   // hyper priority

		TreeSet<MixedTrustTask> set = new TreeSet<MixedTrustTask>(new DecreasingGuestCriticalityPriorityComparator());
		set.add(mt1);
		set.add(mt2);
		set.add(mt3);
		set.add(mt4);
		set.add(mt5);
		
		for (MixedTrustTask t:set){
			System.out.println(t);
		}
	}
}
