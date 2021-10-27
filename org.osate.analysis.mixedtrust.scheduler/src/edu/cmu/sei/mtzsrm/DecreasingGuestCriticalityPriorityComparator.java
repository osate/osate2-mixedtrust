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

public class DecreasingGuestCriticalityPriorityComparator implements Comparator<MixedTrustTask> {

	@Override
	public int compare(MixedTrustTask o1, MixedTrustTask o2) {
		if (o1.getUniqueId() == o2.getUniqueId()) {
			return 0;
		}
		if (o2.getGuestTask().getCritcality() == o1.getGuestTask().getCritcality()){
			if (o1.getPriority() == o2.getPriority()) {
				return (int) (o1.getUniqueId() - o2.getUniqueId());
			} else {
				return o2.getPriority() -o1.getPriority();
			}
		} else {
			return o2.getGuestTask().getCritcality() - o1.getGuestTask().getCritcality();
		}
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
