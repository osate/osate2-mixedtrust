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
