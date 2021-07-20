package edu.cmu.sei.mtzsrm;

public class Unique {

	static long nextUniqueId=0;
	
	long uniqueId = nextUniqueId++;
	
	public long getUniqueId()
	{
		return uniqueId;
	}
}
