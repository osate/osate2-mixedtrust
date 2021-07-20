package edu.cmu.sei.mtzsrm.experiments;

public class ExperimentResult{
	public double avgSchedulable=0.0;
	public double avgDuration=0.0;
	
	public ExperimentResult(double s,double d){
		avgSchedulable=s;
		avgDuration=d;
	}
	
	public ExperimentResult(){
	}
	
	public String toString(){
		return "avgSchedulable:"+avgSchedulable+" avgDuration:"+avgDuration;
	}
}
