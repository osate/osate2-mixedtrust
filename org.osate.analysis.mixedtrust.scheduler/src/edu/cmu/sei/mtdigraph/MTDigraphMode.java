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

import java.io.Serializable;
import java.util.ArrayList;

public class MTDigraphMode implements ActiveContainer, Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = -6918356954995359920L;

	ArrayList<MTTaskGraph> taskset = new ArrayList<MTTaskGraph>();

    public MTDigraphMode(){

    }

    boolean active=true;

    public void setActive(boolean a){
        active = a;
    }

    @Override
	public boolean isActive(){
        return active;
    }

    boolean transitioningOUt=false;

    public void setTransitioningOUt(boolean t){
        transitioningOUt = t;
    }

    public boolean isTransitioningOUt() {
        return transitioningOUt;
    }

    public void addTask(MTTaskGraph g){
        taskset.add(g);

        g.addParentMode(this);
    }

    public double getUtilization(){
        return 0.0;
    }

    public ArrayList<MTTaskGraph> getTaskset() {
        return taskset;
    }
}
