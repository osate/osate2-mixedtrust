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
