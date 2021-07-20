package edu.cmu.sei.mtzsrm;

import java.util.Iterator;
import java.util.TreeSet;

public class LowerPriority implements Iterator<MixedTrustTask> {
	TreeSet<MixedTrustTask> set;
	MixedTrustTask t;
	MixedTrustTask next=null;
	Iterator<MixedTrustTask> iterator;
	
	public boolean hasNext() {
		return next != null;
	}

	public MixedTrustTask next() {
		MixedTrustTask tmp = next;
		if (iterator.hasNext()){
			next = iterator.next();
			if (next.getPriority() >= t.getPriority())
				next = null;
		} else {
			next = null;
		}
		return tmp;
	}

	public LowerPriority(TreeSet<MixedTrustTask> set, MixedTrustTask t){
		this.set = set;
		this.t= t;
		iterator = set.iterator();
		if (iterator.hasNext())
			next = iterator.next();
		if (next.getPriority() >= t.getPriority())
			next = null;
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
		
		LowerPriority iter = new LowerPriority(set, mt1);
		
		MixedTrustTask mt; 
		while (iter.hasNext())
		{
			mt = iter.next();
			System.out.println(mt);
		}
	}

}
