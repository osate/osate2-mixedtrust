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

public class GuestTask {
	
	MixedTrustTask parentTask;
	
	int normalResponseTime;
	
	public void setNormalResponseTime(int r)
	{
		normalResponseTime = r;
	}
	
	public int getNormalResponseTime()
	{
		return normalResponseTime;
	}

	int criticalResponseTime;
	
	public void setCriticalResponseTime(int r)
	{
		criticalResponseTime = r;
	}
	
	public int getCriticalResponseTime()
	{
		return criticalResponseTime;
	}

	int zeroSlack;
	
	public void setZeroSlack(int z)
	{
		zeroSlack = z;
	}
	
	public int getZeroSlack()
	{
		return zeroSlack;
	}
	
	int execNormal;
	
	public void setExecNormal(int n)
	{
		execNormal =n;
	}
	
	public int getExecNormal()
	{
		return execNormal;
	}
	
	int execCritical;
	public void setExecCritical(int c)
	{
		execCritical = c;
	}
	
	public int getExecCritical()
	{
		return execCritical;
	}
	
	public int getPeriod()
	{
		return parentTask.getPeriod();
	}
	
	public int getDeadline()
	{
		return parentTask.getDeadline();
	}
	
	int[] exectime;
	
	public void setExectimes(int []ets)
	{
		exectime = ets;
	}
	
	public void setExectime(int e, int c)
	{
		exectime[c]=e;
	}
	
	public int[] getExectimes()
	{
		return exectime;
	}
	
	public int getExectime(int c)
	{
		if (c<exectime.length)
			return exectime[c];
		else
			return exectime[exectime.length-1];
	}
	
	public int getExectime()
	{
		return exectime[getCritcality()];
	}
	
	int criticality;
	public void setCriticality(int c)
	{
		criticality = c;
	}
	
	public int getCritcality()
	{
		return criticality;
	}
	
	int normalModeSlack=0;
	
	public void setNormalModeSlack(int s){
		normalModeSlack = s;
	}
	
	public int getNormalModeSlack(){
		return normalModeSlack;
	}
	
	int normalModeInterference=0;
	
	public void setNormalModeInterference(int i){
		normalModeInterference = i;
	}
	
	public int getNormalModeInterference(){
		return normalModeInterference;
	}
	
	public GuestTask(int[] exectime, int criticality)
	{
		this.exectime = exectime;
		this.criticality = criticality;
	}

	public GuestTask()
	{
	}
	
	public String toString(){
		String s = "GuestTask[crit:"+this.getCritcality()+" exectime[";
		String r = "";
		for (int i=0;i<this.exectime.length;i++){
			s += r+Integer.toString(this.exectime[i]);
			r =", ";
		}
		s += "] z("+this.getZeroSlack()+"), Cn("+this.getExecNormal()+"), Cc("+this.getExecCritical()+")]";
		return s;
	}
}
