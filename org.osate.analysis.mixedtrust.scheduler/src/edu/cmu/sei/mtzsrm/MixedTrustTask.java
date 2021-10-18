/*******************************************************************************
 * Copyright (c) 2004-2021 Carnegie Mellon University and others. (see Contributors file).
 * All Rights Reserved.
 *
 * NO WARRANTY. ALL MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
 * MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Created, in part, with funding and support from the United States Government. (see Acknowledgments file).
 *
 * This program includes and/or can make use of certain third party source code, object code, documentation and other
 * files ("Third Party Software"). The Third Party Software that is used by this program is dependent upon your system
 * configuration. By using this program, You agree to comply with any and all relevant Third Party Software terms and
 * conditions contained in any such Third Party Software or separate license file distributed with such Third Party
 * Software. The parties who own the Third Party Software ("Third Party Licensors") are intended third party beneficiaries
 * to this license with respect to the terms applicable to their Third Party Software. Third Party Software licenses
 * only apply to the Third Party Software and not any other portion of this program or this program as a whole.
 *******************************************************************************/
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
	
	public String toString()
	{
		return "MixedTrustTask#"+this.getUniqueId()+"(priority:"+priority+" ,period:"+period+", deadline:"+deadline+", enforcer:"+this.getEnforcementTimeout()+", "+this.hyperTask+", "+this.guestTask+")";
	}
}
