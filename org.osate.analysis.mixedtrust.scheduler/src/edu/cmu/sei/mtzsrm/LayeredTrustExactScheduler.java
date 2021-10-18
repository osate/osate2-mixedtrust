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

public class LayeredTrustExactScheduler extends LayeredTrustScheduler {

    public enum Alignment {
		HYPERTASK,
		GUESTTASK
	};

	public int calculateRequestBoundFunction(
					MixedTrustTask ti,
					Alignment align,
					boolean includeGuestPreemption,
					int interval
					){
		int inter=0;
		int includeGuestPreemptionIndicator = (includeGuestPreemption ? 1 : 0);

		if (align == Alignment.HYPERTASK){
			inter = (int)
						(
							this.positiveCeilOrZero
							(
											(interval -
													(ti.getPeriod()-ti.getEnforcementTimeout())
											)
											/((double)ti.getPeriod())
							)
							* ti.getGuestTask().getExectime() * includeGuestPreemptionIndicator
							+ Math.ceil
							(
									interval
									/((double)ti.getPeriod())
							)
							* ti.getHyperTask().getExectime()
						);
		} else { // (align == Alignment.GUESTTASK)
			inter = (int)
						(
							Math.ceil
							(
								interval
								/((double)ti.getPeriod())
							)
							* ti.getGuestTask().getExectime() * includeGuestPreemptionIndicator
							+ this.positiveCeilOrZero
							(
									(interval - ti.getEnforcementTimeout())
									/((double)ti.getPeriod())
							)
							* ti.getHyperTask().getExectime()
						);
		}

		return inter;
	}

	public int calculatePredictiveRequestBoundFunction(
			MixedTrustTask ti,
			Alignment align,
			boolean includeGuestPreemption,
			int interval
			){
		int inter=0;
		int includeGuestPreemptionIndicator = (includeGuestPreemption ? 1 : 0);

		if (align == Alignment.HYPERTASK){
			inter = (int)
					(
							this.positiveCeilOrZero
									(
									(interval -
											(ti.getPeriod()-ti.getEnforcementTimeout())
											)
									/((double)ti.getPeriod())
									)
							* ti.getGuestTask().getExectime() * includeGuestPreemptionIndicator
							+ Math.ceil
									(
									interval
									/((double)ti.getPeriod())
									)
							* ((PredictiveHyperTask)ti.getHyperTask()).getPredictiveExecTime()
							+ calculateFrameInterferenceCorrection(ti, interval)
						);
		} else { // (align == Alignment.GUESTTASK)
			inter = (int)
					(
							Math.ceil
								(
									interval
									/((double)ti.getPeriod())
								)
							* ti.getGuestTask().getExectime() * includeGuestPreemptionIndicator
							+ this.positiveCeilOrZero
								(
									(interval - ti.getEnforcementTimeout())
									/((double)ti.getPeriod())
								)
							* ((PredictiveHyperTask)ti.getHyperTask()).getPredictiveExecTime()
							+ calculateFrameInterferenceCorrection(ti,
									interval-ti.getEnforcementTimeout() >0 ? interval-ti.getEnforcementTimeout():0
											)
							);
		}

		return inter;
	}


	public int calculateGuestBusyPeriod(
			MixedTrustTask ti,
			Alignment align
			){
		int activeP=0;
		int prevActiveP=0;

		// we seed the active period with exectime
		activeP=ti.getGuestTask().getExectime();

		do {
			prevActiveP = activeP;

			activeP=0;

			LowerPriority lp = new LowerPriority(this.increasingHypertaskPriority,ti);
			while (lp.hasNext()){
				MixedTrustTask tj = lp.next();
				activeP += calculateRequestBoundFunction(tj,Alignment.HYPERTASK,false,prevActiveP);
			}

			activeP += calculateRequestBoundFunction(ti,align,true,prevActiveP);

			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				MixedTrustTask tj = hp.next();
				activeP += Math.max(
						calculateRequestBoundFunction(tj,Alignment.HYPERTASK,true,prevActiveP),
						calculateRequestBoundFunction(tj,Alignment.GUESTTASK,true,prevActiveP)
						);
			}
		} while (prevActiveP != activeP);

		return activeP;
	}

	public int calculatePredictiveGuestBusyPeriod(
			MixedTrustTask ti,
			Alignment align
			){
		int activeP=0;
		int prevActiveP=0;

		// we seed the active period with exectime
		activeP=ti.getGuestTask().getExectime();

		do {
			prevActiveP = activeP;

			activeP=0;

			LowerPriority lp = new LowerPriority(this.increasingHypertaskPriority,ti);
			while (lp.hasNext()){
				MixedTrustTask tj = lp.next();
				activeP += calculatePredictiveRequestBoundFunction(tj,Alignment.HYPERTASK,false,prevActiveP);
			}

			activeP += calculatePredictiveRequestBoundFunction(ti,align,true,prevActiveP);

			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				MixedTrustTask tj = hp.next();
				activeP += Math.max(
						calculatePredictiveRequestBoundFunction(tj,Alignment.HYPERTASK,true,prevActiveP),
						calculatePredictiveRequestBoundFunction(tj,Alignment.GUESTTASK,true,prevActiveP)
						);
			}
		} while (prevActiveP != activeP);

		return activeP;
	}

	public int calculateGuestJobFinishingTime(
			MixedTrustTask ti,
			Alignment align,
			int job
			){
		int finishingT=0;
		int prevFinishingT=0;
		int alignedToHyperIndicator = (align == Alignment.HYPERTASK ? 1 : 0);

		// seed finishingT;
		finishingT = ti.getGuestTask().getExectime();

		do {
			prevFinishingT = finishingT;

			finishingT=0;

			LowerPriority lp = new LowerPriority(this.increasingHypertaskPriority,ti);
			while (lp.hasNext()){
				MixedTrustTask tj = lp.next();
				finishingT += calculateRequestBoundFunction(tj,Alignment.HYPERTASK,false,prevFinishingT);
			}

			finishingT += job*ti.getGuestTask().getExectime()
					+ (job - 1 + alignedToHyperIndicator) * ti.getHyperTask().getExectime();

			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				MixedTrustTask tj = hp.next();
				finishingT += Math.max(
						calculateRequestBoundFunction(tj,Alignment.HYPERTASK,true,prevFinishingT),
						calculateRequestBoundFunction(tj,Alignment.GUESTTASK,true,prevFinishingT)
						);

			}

		} while (finishingT != prevFinishingT);

		return finishingT;
	}

	public int calculatePredictiveGuestJobFinishingTime(
			MixedTrustTask ti,
			Alignment align,
			int job
			){
		int finishingT=0;
		int prevFinishingT=0;
		int alignedToHyperIndicator = (align == Alignment.HYPERTASK ? 1 : 0);

		// seed finishingT;
		finishingT = ti.getGuestTask().getExectime();

		do {
			prevFinishingT = finishingT;

			finishingT=0;

			LowerPriority lp = new LowerPriority(this.increasingHypertaskPriority,ti);
			while (lp.hasNext()){
				MixedTrustTask tj = lp.next();
				finishingT += calculatePredictiveRequestBoundFunction(tj,Alignment.HYPERTASK,false,prevFinishingT);
			}

			finishingT += job*ti.getGuestTask().getExectime()
					+ (job - 1 + alignedToHyperIndicator) * ((PredictiveHyperTask)ti.getHyperTask()).getPredictiveExecTime();

			finishingT += this.calculateFrameNumJobsInterferenceCorrection(ti, job -1 + alignedToHyperIndicator);

			HigherPriority hp = new HigherPriority(decreasingHypertaskPriority, ti);
			while (hp.hasNext()){
				MixedTrustTask tj = hp.next();
				finishingT += Math.max(
						calculatePredictiveRequestBoundFunction(tj,Alignment.HYPERTASK,true,prevFinishingT),
						calculatePredictiveRequestBoundFunction(tj,Alignment.GUESTTASK,true,prevFinishingT)
						);

			}

		} while (finishingT != prevFinishingT);

		return finishingT;
	}

	public int calculateGuestJobResponseTime(
			MixedTrustTask ti,
			Alignment align,
			int job
			){
		int R=0;
		int alignToHyperIndicator = (align == Alignment.HYPERTASK ? 1 : 0);

		R = calculateGuestJobFinishingTime(ti,align,job)
				-(
						(job-1)*ti.getPeriod()
						+ alignToHyperIndicator*(ti.getPeriod()-ti.getEnforcementTimeout())
				 );
		return R;
	}

	public int calculatePredictiveGuestJobResponseTime(
			MixedTrustTask ti,
			Alignment align,
			int job
			){
		int R=0;
		int alignToHyperIndicator = (align == Alignment.HYPERTASK ? 1 : 0);

		R = calculatePredictiveGuestJobFinishingTime(ti,align,job)
				-(
						(job-1)*ti.getPeriod()
						+ alignToHyperIndicator*(ti.getPeriod()-ti.getEnforcementTimeout())
				 );
		return R;
	}

	public int calculateGuestTaskResponseTime(
			MixedTrustTask ti,
			Alignment align
			){
		int R=0;
		int maxR=0;
		int activeP=0;
		int numJobs=0;
		int alignToHyperIndicator = (align == Alignment.HYPERTASK ? 1 : 0);

		activeP = calculateGuestBusyPeriod(ti,align);
		numJobs = (int) Math.ceil
							(
									(activeP - alignToHyperIndicator * (ti.getPeriod()-ti.getEnforcementTimeout()))
									/((double)ti.getPeriod())
							);

		for (int q=1 ; q<= numJobs; q++){
			R = calculateGuestJobResponseTime(ti,align,q);
			if (R>maxR){
				maxR = R;
			}
		}

		return maxR;
	}

	public int calculatePredictiveGuestTaskResponseTime(
			MixedTrustTask ti,
			Alignment align
			){
		int R=0;
		int maxR=0;
		int activeP=0;
		int numJobs=0;
		int alignToHyperIndicator = (align == Alignment.HYPERTASK ? 1 : 0);

		activeP = calculatePredictiveGuestBusyPeriod(ti,align);
		numJobs = (int) Math.ceil
							(
									(activeP - alignToHyperIndicator * (ti.getPeriod()-ti.getEnforcementTimeout()))
									/((double)ti.getPeriod())
							);

		for (int q=1 ; q<= numJobs; q++){
			R = calculatePredictiveGuestJobResponseTime(ti,align,q);
			if (R>maxR){
				maxR = R;
			}
		}

		return maxR;
	}

	public int calculateGuestMaxResponseTime(MixedTrustTask ti){
		return Math.max(
				calculateGuestTaskResponseTime(ti,Alignment.GUESTTASK),
				calculateGuestTaskResponseTime(ti,Alignment.HYPERTASK)
				);
	}

	public int calculatePredictiveGuestMaxResponseTime(MixedTrustTask ti){
		return Math.max(
				calculatePredictiveGuestTaskResponseTime(ti,Alignment.GUESTTASK),
				calculatePredictiveGuestTaskResponseTime(ti,Alignment.HYPERTASK)
				);
	}


	@Override
	public boolean isGuesttaskSetSchedulable(){
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			int r = calculateGuestMaxResponseTime(ti);
			ti.getGuestTask().setCriticalResponseTime(r);
			if (r > ti.getEnforcementTimeout()){
				//Logger.addFailure(decreasingHypertaskPriority, ti, true, r);
				return false;
			}
		}

		return true;
	}

	public boolean isPredictiveGuesttaskSetSchedulable(){
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			int r = calculatePredictiveGuestMaxResponseTime(ti);
			ti.getGuestTask().setCriticalResponseTime(r);
			if (r > ti.getEnforcementTimeout()){
				//Logger.addFailure(decreasingHypertaskPriority, ti, true, r);
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isSchedulable()
	{
		// ensure not more than 100%
		double utilization=0.0;
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			utilization += ti.getUtilization();
		}

		if (utilization >=1.0){
			//Logger.addFailure(decreasingHypertaskPriority, null, true,0);
			return false;
		}

		// schedule hyper tasks
		if (!isHypertaskSetSchedulable()){
			//System.out.println("Hypertasks not schedulable");
			return false;
		}

		// schedule guest tasks
		if (!isGuesttaskSetSchedulable()){
			//System.out.println("Guesttasks not schedulable");
			return false;
		}

		return true;
	}

	public boolean isPredictiveSchedulable()
	{
		// ensure not more than 100%
		double utilization=0.0;
		for (MixedTrustTask ti:decreasingHypertaskPriority){
			utilization += ti.getUtilization();
		}

		if (utilization >=1.0){
			//Logger.addFailure(decreasingHypertaskPriority, null, true,0);
			return false;
		}

		// schedule hyper tasks
		if (!isPredictiveHypertaskSetSchedulable()){
			//System.out.println("Hypertasks not schedulable");
			return false;
		}

		// schedule guest tasks
		if (!isGuesttaskSetSchedulable()){
			//System.out.println("Guesttasks not schedulable");
			return false;
		}

		return true;
	}


	public static void testBasicTwoTasks(){
		MixedTrustTask mt1 = new MixedTrustTask(
				8, // period
				8, // deadline
				0, // guest criticality
				new int[] {4}, // guest exectimes
				1, // hyper criticality
				0, // hyper exectime
				1);   // priority
		MixedTrustTask mt2 = new MixedTrustTask(
				14, // period
				14, // deadline
				0, // guest criticality
				new int[] {3}, // guest exectimes
				1, // hyper criticality
				2, // hyper exectime
				0);   // priority

		LayeredTrustExactScheduler sched  = new LayeredTrustExactScheduler();
		sched.add(mt1);
		sched.add(mt2);

		System.out.println("Schedulable = "+sched.isSchedulable());

		System.out.println("Response Times----");
		for (MixedTrustTask ti:sched.decreasingHypertaskPriority){
			System.out.println("Task["+ti.getUniqueId()+"]: gR = "+ti.getGuestTask().getCriticalResponseTime()
					+", hR= "+ti.getHyperTask().getResponseTime());
		}
	}

	public static void testThreeTasks(){
		MixedTrustTask mt1 = new MixedTrustTask(
				8, // period
				8, // deadline
				0, // guest criticality
				new int[] {2}, // guest exectimes
				1, // hyper criticality
				0, // hyper exectime
				2);   // priority
		MixedTrustTask mt2 = new MixedTrustTask(
				14, // period
				14, // deadline
				0, // guest criticality
				new int[] {2}, // guest exectimes
				1, // hyper criticality
				2, // hyper exectime
				1);   // priority
		MixedTrustTask mt3 = new MixedTrustTask(
				20, // period
				20, // deadline
				0, // guest criticality
				new int[] {4}, // guest exectimes
				1, // hyper criticality
				1, // hyper exectime
				0);   // priority

		LayeredTrustExactScheduler sched  = new LayeredTrustExactScheduler();
		sched.add(mt1);
		sched.add(mt2);
		sched.add(mt3);

		System.out.println("Schedulable = "+sched.isSchedulable());

		System.out.println("Response Times----");
		for (MixedTrustTask ti:sched.decreasingHypertaskPriority){
			System.out.println("Task["+ti.getUniqueId()+"]: gR = "+ti.getGuestTask().getCriticalResponseTime()
					+", hR= "+ti.getHyperTask().getResponseTime());
		}

	}


	public static void testThreeTasksNotSchedulable(){
		MixedTrustTask mt1 = new MixedTrustTask(
				8, // period
				8, // deadline
				0, // guest criticality
				new int[] {4}, // guest exectimes
				1, // hyper criticality
				0, // hyper exectime
				2);   // priority
		MixedTrustTask mt2 = new MixedTrustTask(
				14, // period
				14, // deadline
				0, // guest criticality
				new int[] {2}, // guest exectimes
				1, // hyper criticality
				2, // hyper exectime
				1);   // priority
		MixedTrustTask mt3 = new MixedTrustTask(
				20, // period
				20, // deadline
				0, // guest criticality
				new int[] {4}, // guest exectimes
				1, // hyper criticality
				1, // hyper exectime
				0);   // priority

		LayeredTrustExactScheduler sched  = new LayeredTrustExactScheduler();
		sched.add(mt1);
		sched.add(mt2);
		sched.add(mt3);

		System.out.println("Schedulable = "+sched.isSchedulable());

		System.out.println("Response Times----");
		for (MixedTrustTask ti:sched.decreasingHypertaskPriority){
			System.out.println("Task["+ti.getUniqueId()+"]: gR = "+ti.getGuestTask().getCriticalResponseTime()
					+", hR= "+ti.getHyperTask().getResponseTime());
		}

	}

	public static void main(String args[]){
		testBasicTwoTasks();
		testThreeTasks();
		testThreeTasksNotSchedulable();
	}

}
