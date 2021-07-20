package edu.cmu.sei.mtzsrm;

import java.util.Iterator;
import java.util.TreeSet;

public class HigherPriorityLowerGuestCriticality implements Iterator<MixedTrustTask> {
	MixedTrustTask next=null;
	MixedTrustTask ti=null;
	Iterator<MixedTrustTask> iterator;
	
	public boolean hasNext() {
		return next != null;
	}

	void findNext(){
		while(iterator.hasNext()){
			next = iterator.next();
			if (next.getPriority() > ti.getPriority()){
				if (next.getGuestTask().getCritcality()>= ti.getGuestTask().getCritcality()){
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
	
	public MixedTrustTask next() {
		MixedTrustTask tmp = next;
		next = null;
		findNext();
		return tmp;
	}
	
	public HigherPriorityLowerGuestCriticality(TreeSet<MixedTrustTask> set, MixedTrustTask ti){
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
		
		HigherPriorityLowerGuestCriticality hphc = new HigherPriorityLowerGuestCriticality(set,mt1);
		
		MixedTrustTask mt=null;
		
		while(hphc.hasNext()){
			mt = hphc.next();
			System.out.println(mt);
		}
	}
}
