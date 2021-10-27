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

public class MTDigraphModeTransition implements ActiveContainer, Serializable {
	/**
	 * Eclipse-generated id to get rid of warning message.
	 */
	private static final long serialVersionUID = -2101644660378253539L;

	ArrayList<MTTaskGraph> taskTransitions = new ArrayList<MTTaskGraph>();

    MTDigraphMode sourceMode = null;
    MTDigraphMode targetMode = null;

    public MTDigraphModeTransition(MTDigraphMode sourceMode, MTDigraphMode targetMode){
        this.sourceMode = sourceMode;
        this.targetMode = targetMode;
    }

    public void addTaskTransition(MTTaskGraph g){
        taskTransitions.add(g);
        g.addParentMode(this);
    }

    public ArrayList<MTTaskGraph> getTaskTransitions() {
        return taskTransitions;
    }

    public void setSourceMode(MTDigraphMode s){
        sourceMode = s;
    }

    public void setTargetMode(MTDigraphMode t){
        targetMode = t;
    }

    boolean active=true;

    public void setActive(boolean a){
        active = a;
    }

    @Override
	public boolean isActive(){
        return active;
    }
}
