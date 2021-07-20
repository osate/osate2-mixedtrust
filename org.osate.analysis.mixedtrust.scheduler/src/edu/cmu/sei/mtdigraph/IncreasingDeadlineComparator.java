package edu.cmu.sei.mtdigraph;

import java.util.Comparator;
import java.util.TreeSet;

public class IncreasingDeadlineComparator implements Comparator<MTTaskNode> {
    @Override
    public int compare(MTTaskNode t1, MTTaskNode t2) {
        if (t1.getUniqueId()==t2.getUniqueId()){
            return 0;
        }
        if (t1.getDeadline() < t2.getDeadline()){
            return -1;
        }
        if (t1.getDeadline()> t2.getDeadline()){
            return 1;
        }
        return (int)(t1.getUniqueId() - t2.getUniqueId());
    }

    public static void main(String args[]){
        MTTaskNode n1 = new MTTaskNode(1,10,"n1");
        MTTaskNode n2 = new MTTaskNode(1,11,"n2");

        var set = new TreeSet<MTTaskNode>(new IncreasingDeadlineComparator());
        set.add(n1);
        set.add(n2);

        for (MTTaskNode n:set){
            System.out.println(n.getName());
        }
    }
}
