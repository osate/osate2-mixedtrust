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

package edu.cmu.sei.mtzsrm;

import java.util.Comparator;
import java.util.TreeSet;

public class IncreasingPriorityComparator implements Comparator<MixedTrustTask> {

	@Override
	public int compare(MixedTrustTask o1, MixedTrustTask o2) {
		if (o1.getUniqueId() == o2.getUniqueId()) {
			return 0;
		}
		if (o2.getPriority()== o1.getPriority()) {
			return (int) (o1.getUniqueId() - o2.getUniqueId());
		} else {
			return o1.getPriority() - o2.getPriority();
		}
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
