package edu.cmu.sei.mtzsrm;

import java.util.ArrayList;
import java.util.TreeSet;

public class LayeredTrustScheduler {
	
	public int positiveCeilOrZero(double r){
		return ((int)Math.max(0, Math.ceil(r)));
	}
	
	public int positiveFloorOrZero(double r){
		return ((int)Math.max(0, Math.floor(r)));
	}
	
	TreeSet<MixedTrustTask>decreasingHypertaskPriority = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());
	TreeSet<MixedTrustTask>increasingHypertaskPriority = new TreeSet<MixedTrustTask>(new IncreasingPriorityComparator());
	
	public TreeSet<MixedTrustTask> getTaskset(){
		return decreasingHypertaskPriority;
	}
	
	public void assignDeadlineMonotonicPriorities(){
		TreeSet<MixedTrustTask> decreasingDeadlineTaskset = new TreeSet<MixedTrustTask>(new DecreasingDeadlineComparator());
		decreasingDeadlineTaskset.addAll(this.decreasingHypertaskPriority);
		this.decreasingHypertaskPriority.clear();
		this.increasingHypertaskPriority.clear();
		
		int i=0;
		for (MixedTrustTask t:decreasingDeadlineTaskset){
			t.setPriority(i++);
		}
		
		this.decreasingHypertaskPriority.addAll(decreasingDeadlineTaskset);
		this.increasingHypertaskPriority.addAll(decreasingDeadlineTaskset);
	}
	
	public double getUtilization(){
		double util=0.0;
		
		for (MixedTrustTask t:this.decreasingHypertaskPriority){
			util += t.getUtilization();
		}
		
		return util;
	}
	
	public void add(MixedTrustTask t)
	{
		this.decreasingHypertaskPriority.add(t);
		this.increasingHypertaskPriority.add(t);
	}

	public void add(ArrayList<MixedTrustTask> tasks){
		this.decreasingHypertaskPriority.addAll(tasks);
		this.increasingHypertaskPriority.addAll(tasks);
	}
	
	public int calculatePredictiveHyperTaskNonPreemptiveActivePeriod(MixedTrustTask ti)
	{
		int ap=0;
		int prevap=0;
		
		PredictiveHyperTask phti = (PredictiveHyperTask) ti.getHyperTask();
		
		do {
			prevap = ap;
			if (prevap == 0){
				prevap = ap = phti.getPredictiveExecTime();
			} else {
				ap = (int) (Math.ceil(((double)prevap)/((double)ti.getPeriod())) * phti.getPredictiveExecTime());
			}
			ap += calculateFrameInterferenceCorrection(ti, prevap);
			ap += this.getPredictiveMaxLowerPriorityPreemption(ti);
			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				MixedTrustTask mt = hp.next();
				PredictiveHyperTask phtj = (PredictiveHyperTask) mt.getHyperTask();
				ap += ((int)Math.ceil(((double)prevap)/((double)mt.getPeriod()))) * phtj.getPredictiveExecTime();
				ap += calculateFrameInterferenceCorrection(mt, prevap);
			}
		} while (ap != prevap);
		return ap;
	}

	
	public int calculateHyperTaskNonPreemptiveActivePeriod(MixedTrustTask ti)
	{
		int ap=0;
		int prevap=0;
				
		do {
			prevap = ap;
			if (prevap == 0){
				prevap = ap = ti.getHyperTask().getExectime();
			} else {
				ap = (int) (Math.ceil(((double)prevap)/((double)ti.getPeriod())) * ti.getHyperTask().getExectime());
			}
			ap += this.getMaxLowerPriorityPreemption(ti);
			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				MixedTrustTask mt = hp.next();
				ap += ((int)Math.ceil(((double)prevap)/((double)mt.getPeriod()))) * mt.getHyperTask().getExectime();
			}
		} while (ap != prevap);
		return ap;
	}
	
	public int getPredictiveMaxLowerPriorityPreemption(MixedTrustTask ti)
	{
		int c=0;
		LowerPriority lp;
		
		lp = new LowerPriority(increasingHypertaskPriority, ti);
		
		while(lp.hasNext()){
			MixedTrustTask mt = lp.next();
			PredictiveHyperTask pht = (PredictiveHyperTask) mt.getHyperTask();
			if (c<pht.getMaxExecTime()) {
				c = pht.getMaxExecTime();
			}
		}
		
		return c;
	}

	
	public int getMaxLowerPriorityPreemption(MixedTrustTask ti)
	{
		int c=0;
		LowerPriority lp;
		
		lp = new LowerPriority(increasingHypertaskPriority, ti);
		
		while(lp.hasNext()){
			MixedTrustTask mt = lp.next();
			if (c<mt.getHyperTask().getExectime()) {
				c = mt.getHyperTask().getExectime();
			}
		}
		
		return c;
	}
	
	public int calculatePredictiveNonPreemptiveStartingTime(MixedTrustTask ti, int job)
	{
		int w=0;
		int prevw=0;
		int a=0;
		
		do {
			prevw = w;
			w = getPredictiveMaxLowerPriorityPreemption(ti);
			if (job>0) {
				w = w + ((job-1) * ((PredictiveHyperTask)ti.getHyperTask()).getPredictiveExecTime());
				w += calculateFrameNumJobsInterferenceCorrection(ti, job-1);
			}
			if (prevw == 0) {
				prevw = w;
			} 
			
			
			MixedTrustTask tj=null;
			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				tj = hp.next();
				a = (int) Math.floor(((double)prevw)/((double)tj.getPeriod()));
				w += (a+1) * ((PredictiveHyperTask)tj.getHyperTask()).getPredictiveExecTime();
				w += calculateNonPreemptiveFrameInterferenceCorrection(tj,prevw);
			}						
		} while (w != prevw);
		
		return w;
	}

	
	public int calculateNonPreemptiveStartingTime(MixedTrustTask ti, int job)
	{
		int w=0;
		int prevw=0;
		int a=0;
		
		do {
			prevw = w;
			w = getMaxLowerPriorityPreemption(ti);
			w = w + (job * ti.getHyperTask().getExectime());
			if (prevw == 0) {
				prevw = w;
			} 
			
			MixedTrustTask tj=null;
			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				tj = hp.next();
				a = (int) Math.floor(((double)prevw)/((double)tj.getPeriod()));
				w += (a+1) * tj.getHyperTask().getExectime();
			}						
		} while (w != prevw);
		
		return w;
	}
	
	public int calculateFrameInterferenceCorrection(MixedTrustTask tj, int t) {
		int r=0;
		
		if (!(tj.getHyperTask() instanceof PredictiveHyperTask)) {
			return 0;
		}
		
		PredictiveHyperTask pht = (PredictiveHyperTask) tj.getHyperTask();
		
		if (pht.getPredictiveExecTime() > pht.getExectime()) {
			r = - Math.floorDiv(t, tj.getPeriod()*pht.getFramePeriods()) * (pht.getPredictiveExecTime() - pht.getExectime());
			
		} else {
			r = (int) (Math.ceil((double)t/ (double)(tj.getPeriod()*pht.getFramePeriods())) * (pht.getExectime()-pht.getPredictiveExecTime()));
		}
		
		return r;
	}

	public int calculateNonPreemptiveFrameInterferenceCorrection(MixedTrustTask tj, int t) {
		int r=0;
		
		if (!(tj.getHyperTask() instanceof PredictiveHyperTask)) {
			return 0;
		}
		
		PredictiveHyperTask pht = (PredictiveHyperTask) tj.getHyperTask();
		
		if (pht.getPredictiveExecTime() > pht.getExectime()) {
			r = - Math.floorDiv(t, tj.getPeriod()*pht.getFramePeriods()) * (pht.getPredictiveExecTime() - pht.getExectime());
			
		} else {
			r = (Math.floorDiv(t,tj.getPeriod()*pht.getFramePeriods())+1) * (pht.getExectime()-pht.getPredictiveExecTime());
		}
		
		return r;
	}

	public int calculateFrameNumJobsInterferenceCorrection(MixedTrustTask tj, int t) {
		int r;
		
		if (!(tj.getHyperTask() instanceof PredictiveHyperTask)) {
			return 0;
		}
		
		PredictiveHyperTask pht = (PredictiveHyperTask) tj.getHyperTask();

		if (pht.getPredictiveExecTime() > pht.getExectime()) {
			r = - Math.floorDiv(t,pht.getFramePeriods()) * (pht.getPredictiveExecTime() - pht.getExectime());

		} else {
			r = (int) (Math.ceil((double)t/ (double)(pht.getFramePeriods())) * (pht.getExectime()-pht.getPredictiveExecTime()));

		}
		return r;
	}
	
	// HERE: just finished this modification. Now look for the guest task calculation
	public int calculatePredictiveHypertaskNonPreemptiveResponseTime(MixedTrustTask ti)
	{
		int r=0;
		int maxR=0;
		int a=0;
		int numJobs=0;
		int w=0;
		
		a = this.calculatePredictiveHyperTaskNonPreemptiveActivePeriod(ti);
		
		numJobs = (int) Math.ceil(((double)a)/((double)ti.getPeriod()));
		
		for (int i=0;i<numJobs;i++){
			w = this.calculatePredictiveNonPreemptiveStartingTime(ti, i); 
			w -= (i * ti.getPeriod());
			r = ((PredictiveHyperTask)ti.getHyperTask()).getPredictiveExecTime() >0 ? w + ((PredictiveHyperTask)ti.getHyperTask()).getPredictiveExecTime():0;
			if (r > maxR) {
				maxR = r;
			}
		}
		
		return maxR;
	}

	
	public int calculateHypertaskNonPreemptiveResponseTime(MixedTrustTask ti)
	{
		int r=0;
		int maxR=0;
		int a=0;
		int numJobs=0;
		int w=0;
		
		a = this.calculateHyperTaskNonPreemptiveActivePeriod(ti);
		
		numJobs = (int) Math.ceil(((double)a)/((double)ti.getPeriod()));
		
		for (int i=0;i<numJobs;i++){
			w = this.calculateNonPreemptiveStartingTime(ti, i); 
			w -= (i * ti.getPeriod());
			r = ti.getHyperTask().getExectime() >0 ? w + ti.getHyperTask().getExectime():0;
			if (r > maxR) {
				maxR = r;
			}
		}
		
		return maxR;
	}
	
	public int calculateGuestTaskResponseTime(MixedTrustTask ti)
	{
		int r=0;
		int prevR=0;
		
		do{
			prevR = r;
			r = ti.getGuestTask().getExectime(ti.getGuestTask().getCritcality());
			if (prevR == 0){
				prevR = r;
			}
			// all hypertasks
			for (MixedTrustTask tj:this.decreasingHypertaskPriority){
				// except ti
				if (tj.getUniqueId() == ti.getUniqueId()) {
					continue;
				}
				r += ((int)Math.ceil(((double)prevR)/((double)tj.getPeriod()))) * tj.getHyperTask().getExectime();
			}
			
			// all higher priority guest tasks
			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority,ti);
			while(hp.hasNext()){
				MixedTrustTask tj = hp.next();
				r += ((int)Math.ceil(((double)prevR)/((double)tj.getPeriod()))) * tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality());
			}
		} while(prevR != r && r <= ti.getDeadline());
		
		return r;
	}
	
	public boolean isPredictiveHypertaskSetSchedulable(){
		int r=0;
		// schedule hyper tasks
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			r = calculatePredictiveHypertaskNonPreemptiveResponseTime(ti);
			ti.getHyperTask().setResponseTime(r);
			if (r> ti.getDeadline()){
				//Logger.addFailure(decreasingHypertaskPriority, ti, false,r);
				return false;
			}
		}
		return true;
	}
	
	public boolean isHypertaskSetSchedulable(){
		int r=0;
		// schedule hyper tasks
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			r = calculateHypertaskNonPreemptiveResponseTime(ti);
			ti.getHyperTask().setResponseTime(r);
			if (r> ti.getDeadline()){
				//Logger.addFailure(decreasingHypertaskPriority, ti, false,r);
				return false;
			}
		}
		return true;
	}
	
	public boolean isGuesttaskSetSchedulable(){
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			int r = calculateGuestTaskResponseTime(ti);
			ti.getGuestTask().setNormalResponseTime(r);
			if (r > ti.getEnforcementTimeout()){
				//Logger.addFailure(decreasingHypertaskPriority, ti, false,r);
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isSchedulable()
	{
		// schedule hyper tasks
		
		if (!isHypertaskSetSchedulable()){
			return false;
		}

		// schedule guest tasks
		if (!isGuesttaskSetSchedulable()) {
			return false;
		}
		
		return true;
	}
	
	public static void testCanbusExample()
	{
		MixedTrustTask mt1 = new MixedTrustTask(
				250, // period
				250, // deadline
				1, // guest criticality
				new int[] {0}, // guest exectimes
				1, // hyper criticality
				100, // hyper exectime
				3);   // hyper priority
		MixedTrustTask mt2 = new MixedTrustTask(
				350, // period
				325, // deadline
				1, // guest criticality
				new int[] {0}, // guest exectimes
				1, // hyper criticality
				100, // hyper exectime
				2);   // hyper priority
		MixedTrustTask mt3 = new MixedTrustTask(
				350, // period
				325, // deadline
				1, // guest criticality
				new int[] {0}, // guest exectimes
				1, // hyper criticality
				100, // hyper exectime
				1);   // hyper priority
		
		LayeredTrustScheduler sched  = new LayeredTrustScheduler();
		sched.add(mt1);
		sched.add(mt2);
		sched.add(mt3);
				
		System.out.println("Response Times----");
		for (MixedTrustTask ti:sched.decreasingHypertaskPriority){
			int r = sched.calculateHypertaskNonPreemptiveResponseTime(ti);
			System.out.println(r);
		}
	}
	
	public static void testLayeredTrust()
	{
		MixedTrustTask mt1 = new MixedTrustTask(
				100, // period
				100, // deadline
				0, // guest criticality
				new int[] {10}, // guest exectimes (crit -> exectime)
				1, // hyper criticality
				5, // hyper exectime
				3);   // priority
		MixedTrustTask mt2 = new MixedTrustTask(
				200, // period
				200, // deadline
				0, // guest criticality
				new int[] {100}, // guest exectimes (crit -> exectime)
				1, // hyper criticality
				5, // hyper exectime
				2);   // priority
		MixedTrustTask mt3 = new MixedTrustTask(
				400, // period
				400, // deadline
				0, // guest criticality
				new int[] {40}, // guest exectimes (crit -> exectime)
				1, // hyper criticality
				5, // hyper exectime
				1);   // priority
		LayeredTrustScheduler sched  = new LayeredTrustScheduler();
		sched.add(mt1);
		sched.add(mt2);
		sched.add(mt3);

		System.out.println("Schedulable = "+sched.isSchedulable());
		for (MixedTrustTask ti:sched.decreasingHypertaskPriority){
			int r = ti.getGuestTask().getNormalResponseTime();//sched.calculateGuestTaskResponseTime(ti);
			System.out.println(r);
		}
	}
	
	public static void main(String args[])
	{
		testLayeredTrust();
	}
}
