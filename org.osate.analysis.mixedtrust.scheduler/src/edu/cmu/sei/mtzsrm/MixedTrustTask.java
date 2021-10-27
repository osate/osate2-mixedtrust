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

public class MixedTrustTask extends Unique{

	public int getEnforcementTimeout()
	{
		return getDeadline() - hyperTask.getResponseTime();
	}

	int priority=0;

	public void setPriority(int p)
	{
		priority = p;
	}

	public int getPriority()
	{
		return priority;
	}

	int period;

	public void setPeriod(int p)
	{
		period = p;
	}

	public int getPeriod()
	{
		return period;
	}
	int deadline;
	public void setDeadline(int d)
	{
		deadline = d;
	}

	public int getDeadline()
	{
		return deadline;
	}

	HyperTask hyperTask;

	public HyperTask getHyperTask()
	{
		return hyperTask;
	}

	GuestTask guestTask;

	public GuestTask getGuestTask()
	{
		return guestTask;
	}

	public double getUtilization(){
		return (this.getGuestTask().getExectime()+this.getHyperTask().getExectime()) / ((double)this.getPeriod());
	}

	public MixedTrustTask(int period, int deadline, GuestTask gt, HyperTask ht)
	{
		this.period = period;
		this.deadline = deadline;
		ht.parentTask = this;
		gt.parentTask = this;
		hyperTask = ht;
		guestTask = gt;
	}

	public MixedTrustTask(int period, int deadline, int gestCriticality, int[] guestExectimes,
			int hyperCriticality, int hyperExectime)
	{
		guestTask = new GuestTask(guestExectimes, gestCriticality);
		hyperTask = new HyperTask(hyperCriticality, hyperExectime);
		this.period = period;
		this.deadline = deadline;
		hyperTask.parentTask = this;
		guestTask.parentTask = this;
	}

	public MixedTrustTask(int period, int deadline, int gestCriticality, int[] guestExectimes,
			int hyperCriticality, int hyperExectime, int priority)
	{
		this(period, deadline, gestCriticality, guestExectimes,
			hyperCriticality, hyperExectime);
		this.priority = priority;
	}

	public MixedTrustTask()
	{
	}

	@Override
	public String toString()
	{
		return "MixedTrustTask#"+this.getUniqueId()+"(priority:"+priority+" ,period:"+period+", deadline:"+deadline+", enforcer:"+this.getEnforcementTimeout()+", "+this.hyperTask+", "+this.guestTask+")";
	}
}
