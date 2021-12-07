package edu.cmu.sei.mtzsrm;

public class PredictiveHyperTask extends HyperTask {
	int predictiveExecTime;
	
	public void setPredictiveExecTime(int p) {
		predictiveExecTime = p;
	}
	
	public int getPredictiveExecTime() {
		return predictiveExecTime;
	}

	public int getMaxExecTime() {
		return (predictiveExecTime > exectime)? predictiveExecTime : exectime;
	}
	
	int framePeriods;
	
	public void setFramePeriods(int f) {
		framePeriods = f;
	}
	
	public int getFramePeriods() {
		return framePeriods;
	}
	
	public PredictiveHyperTask(int criticality, int exectime, int pet, int framePeriods)
	{
		super(criticality,exectime);
		this.predictiveExecTime = pet;
		this.framePeriods = framePeriods;
	}

	public PredictiveHyperTask() {
		super();
	}
}
