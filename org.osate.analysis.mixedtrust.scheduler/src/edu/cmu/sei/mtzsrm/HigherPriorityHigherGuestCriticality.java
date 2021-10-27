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

import java.util.Iterator;
import java.util.TreeSet;

public class HigherPriorityHigherGuestCriticality implements Iterator<MixedTrustTask> {
	MixedTrustTask next=null;
	MixedTrustTask ti=null;
	Iterator<MixedTrustTask> iterator;

	@Override
	public boolean hasNext() {
		return next != null;
	}

	void findNext(){
		while(iterator.hasNext()){
			next = iterator.next();
			if (next.getPriority() > ti.getPriority()){
				if (next.getGuestTask().getCritcality()<= ti.getGuestTask().getCritcality()){
					next = null;
					continue;
				} else {
					break;
				}
			} else {
				next = null;
				break;
			}
		}
	}

	@Override
	public MixedTrustTask next() {
		MixedTrustTask tmp = next;
		next = null;
		findNext();
		return tmp;
	}

	public HigherPriorityHigherGuestCriticality(TreeSet<MixedTrustTask> set, MixedTrustTask ti){
		this.ti = ti;
		this.iterator = set.iterator();
		findNext();
	}

	public static void main(String args[]){
		TreeSet<MixedTrustTask> set = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());

		MixedTrustTask mt1 = new MixedTrustTask(
				100, 	// period
				100, 	// deadline
				2, 		// guest criticality
				new int[] {10}, // guest exectimes (crit -> exectime)
				1, 		// hyper criticality
				5, 		// hyper exectime
				3);   	// priority
		MixedTrustTask mt2 = new MixedTrustTask(
				100, 	// period
				100, 	// deadline
				0, 		// guest criticality
				new int[] {10}, // guest exectimes (crit -> exectime)
				1, 		// hyper criticality
				5, 		// hyper exectime
				2);   	// priority
		MixedTrustTask mt3 = new MixedTrustTask(
				100, 	// period
				100, 	// deadline
				1, 		// guest criticality
				new int[] {10}, // guest exectimes (crit -> exectime)
				2, 		// hyper criticality
				5, 		// hyper exectime
				1);   	// priority

		set.add(mt1);
		set.add(mt2);
		set.add(mt3);

		HigherPriorityHigherGuestCriticality hphc = new HigherPriorityHigherGuestCriticality(set,mt3);

		MixedTrustTask mt=null;

		while(hphc.hasNext()){
			mt = hphc.next();
			System.out.println(mt);
		}
	}
}
