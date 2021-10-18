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

public class IncreasingPriorityComparator implements Comparator<MixedTrustTask> {

	public int compare(MixedTrustTask o1, MixedTrustTask o2) {
		if (o1.getUniqueId() == o2.getUniqueId())
			return 0;
		if (o2.getPriority()== o1.getPriority())
			return (int) (o1.getUniqueId() - o2.getUniqueId());
		else
			return o1.getPriority() - o2.getPriority();
	}

	public static void main(String args[])
	{
		TreeSet<MixedTrustTask> set = new TreeSet<MixedTrustTask>(new IncreasingPriorityComparator());

		HyperTask ht1 = new HyperTask(3,5);
		GuestTask gt1 = new GuestTask(new int[]{1,1,1},1);
		MixedTrustTask mt1 = new MixedTrustTask(100,100,gt1,ht1);
		mt1.setPriority(3);

		HyperTask ht2 = new HyperTask(3,5);
		GuestTask gt2 = new GuestTask(new int[]{1,2,1},2);
		MixedTrustTask mt2 = new MixedTrustTask(200,200,gt2,ht2);
		mt2.setPriority(2);

		HyperTask ht3 = new HyperTask(3,5);
		GuestTask gt3 = new GuestTask(new int[]{1,2,3},3);
		MixedTrustTask mt3 = new MixedTrustTask(300,300,gt3,ht3);
		mt3.setPriority(1);

		set.add(mt1);
		set.add(mt2);
		set.add(mt3);

		for (MixedTrustTask mt:set)
		{
			System.out.println(mt);
		}
	}
}
