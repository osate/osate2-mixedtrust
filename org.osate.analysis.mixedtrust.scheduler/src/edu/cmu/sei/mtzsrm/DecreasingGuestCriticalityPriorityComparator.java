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
