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
	
	public String toString()
	{
		return "HyperTask[crit:"+this.getCriticality()+", exectime: "+this.getExectime()+"]";
	}
}
