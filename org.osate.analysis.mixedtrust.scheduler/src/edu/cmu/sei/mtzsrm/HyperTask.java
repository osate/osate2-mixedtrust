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

public class HyperTask {

	MixedTrustTask parentTask;

	int responseTime;

	public void setResponseTime(int r)
	{
		responseTime = r;
	}

	public int getResponseTime()
	{
		return responseTime;
	}

	public int getPeriod()
	{
		return parentTask.getPeriod();
	}

	public int getDeadline()
	{
		return parentTask.getDeadline();
	}

	int criticality;
	public void setCriticality(int c)
	{
		criticality = c;
	}
	public int getCriticality()
	{
		return criticality;
	}
	int exectime;
	public void setExectime(int e)
	{
		exectime = e;
	}
	public int getExectime()
	{
		return exectime;
	}

	public HyperTask(int criticality, int exectime)
	{
		this.criticality=criticality;
		this.exectime = exectime;
	}

	public HyperTask()
	{
	}

	@Override
	public String toString()
	{
		return "HyperTask[crit:"+this.getCriticality()+", exectime: "+this.getExectime()+"]";
	}
}
