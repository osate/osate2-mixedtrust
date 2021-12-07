package edu.cmu.sei.mtzsrm;

import java.util.Comparator;
import java.util.TreeSet;

public class DecreasingPriorityComparator implements Comparator<MixedTrustTask> {
	public int compare(MixedTrustTask o1, MixedTrustTask o2) {
		if (o1.getUniqueId() == o2.getUniqueId())
			return 0;
		if (o2.getPriority() == o1.getPriority())
			return (int) (o1.getUniqueId() - o2.getUniqueId());
		else
			return o2.getPriority() -o1.getPriority();
	}

	public static void main(String args[])
	{
		TreeSet<MixedTrustTask> set = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());
		
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
