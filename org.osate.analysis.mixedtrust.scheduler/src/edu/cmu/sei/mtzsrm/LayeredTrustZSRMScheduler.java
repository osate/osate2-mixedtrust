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

import java.util.TreeSet;

public class LayeredTrustZSRMScheduler extends LayeredTrustScheduler {

	public TreeSet<MixedTrustTask> getDelayableHigherPriorityHigherCriticality(TreeSet<MixedTrustTask> set, MixedTrustTask ti){
		TreeSet<MixedTrustTask>subset = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());

		TreeSet<MixedTrustTask> tihplc = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());
		HigherPriorityLowerGuestCriticality tihplci = new HigherPriorityLowerGuestCriticality(set,ti);
		while (tihplci.hasNext()) {
			tihplc.add(tihplci.next());
		}

		HigherPriorityHigherSameGuestCriticality tihphsci = new HigherPriorityHigherSameGuestCriticality(set,ti);
		while (tihphsci.hasNext()){
			MixedTrustTask tj = tihphsci.next();
			TreeSet<MixedTrustTask> tjhplc = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());
			HigherPriorityLowerGuestCriticality tjhplci = new HigherPriorityLowerGuestCriticality(set,tj);
			while(tjhplci.hasNext()) {
				tjhplc.add(tjhplci.next());
			}
			tjhplc.retainAll(tihplc);
			if (!tjhplc.isEmpty()){
				subset.add(tj);
			}
		}

		return subset;
	}

	/**
	 * This should never be called with a C_i^c == 0
	 * @param ti
	 * @return
	 */
	public int calculateCriticalResponseTime(MixedTrustTask ti){
		int r=0;
		int prevR=0;

		if (ti.getGuestTask().getExecCritical() < 0) {
			throw new IllegalArgumentException("ExecCritical <0");
		}
		if (ti.getGuestTask().getExecCritical() == 0) {
			return 0;
		}

		do {
			prevR = r;

			// own execution in critical mode
			r = ti.getGuestTask().getExecCritical();
			if (prevR == 0){
				prevR = r;
			}

			// Lower priority hyper-task preemptions
			LowerPriority klp = new LowerPriority(this.increasingHypertaskPriority,ti);
			while (klp.hasNext()){
				MixedTrustTask tj = klp.next();
				r += positiveCeilOrZero(((double)prevR)/((double)tj.getPeriod()))
						* tj.getHyperTask().getExectime();
			}

			// Delayable Higher Priority Higher Criticality preemptions (with carry in)
			TreeSet<MixedTrustTask> delayableHPLC = getDelayableHigherPriorityHigherCriticality(this.decreasingHypertaskPriority,ti);
			for(MixedTrustTask tj:delayableHPLC){
				r += (Math.max(
						positiveCeilOrZero(
								(prevR -(tj.getPeriod()-tj.getEnforcementTimeout())
								)
								/((double)tj.getPeriod()))
								* tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality())
								+positiveCeilOrZero(prevR/((double)tj.getPeriod())) * tj.getHyperTask().getExectime()
						,
						positiveCeilOrZero(
								(prevR+tj.getGuestTask().getZeroSlack()+tj.getGuestTask().getCriticalResponseTime()
								-tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality())
								)
								/((double)tj.getPeriod())
								)
						*tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality())
						+positiveCeilOrZero(
								(prevR+tj.getGuestTask().getZeroSlack()+tj.getGuestTask().getCriticalResponseTime()
								-tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality())
								)-tj.getEnforcementTimeout()
								/((double)tj.getPeriod())
								)
						*tj.getHyperTask().getExectime()
						));
			}

			// Non-Delayable higher priority higher or same criticality
			TreeSet<MixedTrustTask>hphsc = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());
			hphsc.addAll(this.decreasingHypertaskPriority);
			hphsc.removeAll(delayableHPLC);
			HigherPriorityHigherSameGuestCriticality hphsci = new HigherPriorityHigherSameGuestCriticality(hphsc,ti);
			while(hphsci.hasNext()){
				MixedTrustTask tj = hphsci.next();
				r += (Math.max(
						positiveCeilOrZero(
								(prevR - (tj.getPeriod() - tj.getEnforcementTimeout())
								)
								/((double)tj.getPeriod()))
						*tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality())
						+positiveCeilOrZero(prevR/((double)tj.getPeriod())
								)
						*tj.getHyperTask().getExectime()
						,
						positiveCeilOrZero(prevR/((double)tj.getPeriod())
								)
						* tj.getGuestTask().getExectime(tj.getGuestTask().getCritcality())
						+ positiveCeilOrZero((prevR-tj.getEnforcementTimeout())
								/((double)tj.getPeriod()))
						));
			}

			// Lower priority higher criticality
			LowerPriorityHigherGuestCriticality lphc = new LowerPriorityHigherGuestCriticality(this.increasingHypertaskPriority,ti);
			while(lphc.hasNext()){
				MixedTrustTask tj = lphc.next();
				r += (Math.max(
						0,
						tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
						-tj.getGuestTask().getExecNormal()
						));
			}
		} while (prevR != r && r <= ti.getEnforcementTimeout());
		return r;
	}

	public int calculateNormalModeInterference(MixedTrustTask ti){
		int I=0;

		// Lower priority hyper-task preemptions
		LowerPriority klp = new LowerPriority(this.increasingHypertaskPriority,ti);
		while (klp.hasNext()){
			MixedTrustTask tj = klp.next();
			I += ((int)Math.ceil(ti.getGuestTask().getZeroSlack()/((double)tj.getPeriod()))
					*tj.getHyperTask().getExectime());
		}

		int [] Ii = new int[4];
		HigherPriorityLowerGuestCriticality hplc = new HigherPriorityLowerGuestCriticality(this.decreasingHypertaskPriority,ti);
		while(hplc.hasNext()){
			MixedTrustTask tj = hplc.next();

			Ii[0] = calculateInterferenceInterleaving1(ti,tj);
			Ii[1] = calculateInterferenceInterleaving2(ti,tj);
			// not applicable for lower-criticality tj
			//Ii[2] = calculateInterferenceInterleaving3(ti,tj);
			Ii[2] = calculateInterferenceInterleaving4(ti,tj);
			// Not applicable for lower-criticality tj
			//Ii[4] = calculateInterferenceInterleaving5(ti,tj);
			Ii[3] = calculateInterferenceInterleaving6(ti,tj);

			int max = Ii[0];
			for (int i=1;i<Ii.length;i++){
				if (max < Ii[i]) {
					max = Ii[i];
				}
			}
			I += max;
		}

		HigherPriorityHigherSameGuestCriticality hphsc = new HigherPriorityHigherSameGuestCriticality(this.decreasingHypertaskPriority,ti);
		while(hphsc.hasNext()){
			MixedTrustTask tj = hphsc.next();
			Ii[0] = calculateInterferenceInterleaving1(ti,tj);
			Ii[1] = calculateInterferenceInterleaving2(ti,tj);
			//Ii[2] = calculateInterferenceInterleaving3(ti,tj);
			int max = Ii[0];
			for (int i=1;i<2;i++){
				if (max < Ii[i]) {
					max = Ii[i];
				}
			}
			I += max;
		}
		return I;
	}

	private int calculateInterferenceInterleaving6(MixedTrustTask ti, MixedTrustTask tj) {
		int I=0;

		I = tj.getHyperTask().getExectime()
			+ positiveCeilOrZero(
				(ti.getGuestTask().getZeroSlack()
				 -(tj.getPeriod()
					-(tj.getEnforcementTimeout()
					  +tj.getHyperTask().getResponseTime()
					  -tj.getHyperTask().getExectime()
					 )
				  )
				)
				/ ((double)tj.getPeriod())
				)
			*tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
			+ positiveCeilOrZero(
					(ti.getGuestTask().getZeroSlack()
					  +(tj.getHyperTask().getResponseTime()
						-tj.getHyperTask().getExectime()
					   )
					  -tj.getPeriod()
					)
					/ ((double)tj.getPeriod())
					)
			* tj.getHyperTask().getExectime();
		return I;
	}

	@SuppressWarnings("unused")
	private int calculateInterferenceInterleaving5(MixedTrustTask ti, MixedTrustTask tj) {
		int I=0;

		I = tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
			- tj.getGuestTask().getExecNormal()
			+ positiveCeilOrZero(
					(ti.getGuestTask().getZeroSlack()
					-(
					tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
					- tj.getGuestTask().getExecNormal()
					)
					-(
					tj.getPeriod()
					-tj.getEnforcementTimeout()
					)
					)
					/ ((double)tj.getPeriod())
				)
				* tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
			+ positiveCeilOrZero(
					(ti.getGuestTask().getZeroSlack()
					  -(tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
						-tj.getGuestTask().getExecNormal()
						)
					)
					/ ((double)tj.getPeriod())
				)
				*tj.getHyperTask().getExectime();
		return I;
	}

	private int calculateInterferenceInterleaving4(MixedTrustTask ti, MixedTrustTask tj) {
		int I=0;

		I = positiveCeilOrZero(
				(ti.getGuestTask().getZeroSlack()
					+ tj.getGuestTask().getNormalResponseTime()
					-tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
				)
				/ tj.getPeriod()
				)
			* tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
			+ positiveCeilOrZero(
					(ti.getGuestTask().getZeroSlack()
					 +tj.getGuestTask().getNormalResponseTime()
					 -tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
					 -tj.getEnforcementTimeout()
					)
					/tj.getPeriod()
					)
			*tj.getHyperTask().getExectime();

		return I;
	}

	@SuppressWarnings("unused")
	private int calculateInterferenceInterleaving3(MixedTrustTask ti, MixedTrustTask tj) {
		int I=0;

		I = tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
				- tj.getGuestTask().getExecNormal()
				+ tj.getHyperTask().getExectime()
				+ positiveCeilOrZero(
						(ti.getGuestTask().getZeroSlack()
						-(tj.getPeriod()-tj.getGuestTask().getZeroSlack()))
						/ ((double)tj.getPeriod())
					)
				*tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
				+ positiveCeilOrZero(
						(ti.getGuestTask().getZeroSlack()
						-(tj.getPeriod()
						  -tj.getGuestTask().getZeroSlack()
						 )
						-tj.getEnforcementTimeout()
						)
						/((double)tj.getPeriod())
					)
				*tj.getHyperTask().getExectime();

		return I;
	}

	private int calculateInterferenceInterleaving2(MixedTrustTask ti, MixedTrustTask tj) {
		int I=0;

		I = (positiveCeilOrZero((ti.getGuestTask().getZeroSlack()
				-(tj.getPeriod()-tj.getEnforcementTimeout()))
				/ ((double)tj.getPeriod())
				)
				* tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
				+
				((int)Math.ceil(ti.getGuestTask().getZeroSlack()
						/((double)tj.getPeriod())))
				*tj.getHyperTask().getExectime()
				);
		return I;
	}

	private int calculateInterferenceInterleaving1(MixedTrustTask ti, MixedTrustTask tj) {
		int I=0;

		I = (((int)Math.ceil(ti.getGuestTask().getZeroSlack()
				/((double)tj.getPeriod())))
				*tj.getGuestTask().getExectime(ti.getGuestTask().getCritcality())
				)
				+
				(positiveCeilOrZero((ti.getGuestTask().getZeroSlack()
						-tj.getEnforcementTimeout())
						/((double)tj.getPeriod()))
				*tj.getHyperTask().getExectime()
				);

		return I;
	}

	public int calculateNormalModeSlack(MixedTrustTask ti){
		int S=0;
		int I = this.calculateNormalModeInterference(ti);

		ti.getGuestTask().setNormalModeInterference(I);

		S = Math.max(0,
					ti.getGuestTask().getZeroSlack()
					-I
					-ti.getGuestTask().getExecNormal()
				    );
		ti.getGuestTask().setNormalModeSlack(S);
		return S;
	}

	public int recalculateNormalExectime(MixedTrustTask ti){
		int C=0;
		C = Math.min(ti.getGuestTask().getExectime(ti.getGuestTask().getCritcality()),
				ti.getGuestTask().getExecNormal()
				+ti.getGuestTask().getNormalModeSlack());
		ti.getGuestTask().setExecNormal(C);
		return C;
	}

	public int recalculateCriticalExectime(MixedTrustTask ti){
		int C=0;
		C = Math.max(0,
				ti.getGuestTask().getExecCritical()
				-ti.getGuestTask().getNormalModeSlack());
		ti.getGuestTask().setExecCritical(C);
		return C;
	}

	public int recalculateNormalResponseTime(MixedTrustTask ti){
		int r = ti.getGuestTask().getExecNormal()+ti.getGuestTask().getNormalModeInterference();
		ti.getGuestTask().setNormalResponseTime(r);
		return r;
	}

	@Override
	public boolean isSchedulable(){

		// schedule hypertasks
		if (!isHypertaskSetSchedulable()){
			return false;
		}

		for (MixedTrustTask t:this.increasingHypertaskPriority){
			t.getGuestTask().setCriticalResponseTime(0);
			t.getGuestTask().setNormalResponseTime(t.getEnforcementTimeout());
			t.getGuestTask().setNormalModeInterference(0);
			t.getGuestTask().setNormalModeSlack(0);
			t.getGuestTask().setExecCritical(t.getGuestTask().getExectime(t.getGuestTask().getCritcality()));
			t.getGuestTask().setExecNormal(0);
		}


		TreeSet<MixedTrustTask> decCrit = new TreeSet<MixedTrustTask>(new DecreasingGuestCriticalityPriorityComparator());
		decCrit.addAll(this.decreasingHypertaskPriority);

		// schedule tasks in decreasing order of guest criticality and then priority
		for (MixedTrustTask ti:decCrit){
			// zero-slack recurrent loop
			int Z=0;
			int prevZ=0;

			do {
				prevZ = Z;
				ti.getGuestTask().setCriticalResponseTime(this.calculateCriticalResponseTime(ti));
				Z = ti.getEnforcementTimeout() - ti.getGuestTask().getCriticalResponseTime();
				if (Z>=0){
					ti.getGuestTask().setZeroSlack(Z);
					ti.getGuestTask().setNormalModeInterference(this.calculateNormalModeInterference(ti));
					ti.getGuestTask().setNormalModeSlack(Math.max(0,
							ti.getGuestTask().getZeroSlack()
							- ti.getGuestTask().getNormalModeInterference()
							- ti.getGuestTask().getExecNormal()
							)
							);
					ti.getGuestTask().setExecCritical(this.recalculateCriticalExectime(ti));
					ti.getGuestTask().setExecNormal(this.recalculateNormalExectime(ti));
				}
				ti.getGuestTask().setNormalResponseTime(this.recalculateNormalResponseTime(ti));
			} while (prevZ != Z && Z >=0);

			if (Z <0){
				return false;
			}
		}

		return true;
	}

	public static void testCalculateDelayableSet(){
		MixedTrustTask mt1 = new MixedTrustTask(
				100, // period
				100, // deadline
				5, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				1);   // hyper priority
		MixedTrustTask mt2 = new MixedTrustTask(
				100, // period
				100, // deadline
				5, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				2);   // hyper priority
		MixedTrustTask mt3 = new MixedTrustTask(
				100, // period
				100, // deadline
				4, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				3);   // hyper priority
		MixedTrustTask mt4 = new MixedTrustTask(
				100, // period
				100, // deadline
				5, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				4);   // hyper priority
		MixedTrustTask mt5 = new MixedTrustTask(
				100, // period
				100, // deadline
				1, // guest criticality
				new int[] {10}, // guest exectimes
				1, // hyper criticality
				10, // hyper exectime
				5);   // hyper priority

		TreeSet<MixedTrustTask> set = new TreeSet<MixedTrustTask>(new DecreasingPriorityComparator());
		set.add(mt1);
		set.add(mt2);
		set.add(mt3);
		set.add(mt4);
		set.add(mt5);

		LayeredTrustZSRMScheduler sched = new LayeredTrustZSRMScheduler();
		TreeSet<MixedTrustTask> delayable = sched.getDelayableHigherPriorityHigherCriticality(set, mt3);

		for (MixedTrustTask t:delayable){
			System.out.println(t);
		}
	}

	public static void testPositiveCeilOrZero(){
		LayeredTrustZSRMScheduler sched = new LayeredTrustZSRMScheduler();
		System.out.println("ceil(-1.1/1): "+sched.positiveCeilOrZero(-1.1d/1.0d));
		System.out.println("ceil(-1/1): "+sched.positiveCeilOrZero(-1.0d/1.0d));
		System.out.println("ceil(-0.9/1): "+sched.positiveCeilOrZero(-0.9/1.0d));
		System.out.println("ceil(0.1/1): "+sched.positiveCeilOrZero(0.1/1.0d));
	}

	public static void testBasicTwoTasks(){
		MixedTrustTask mt1 = new MixedTrustTask(
				400, // period
				400, // deadline
				0, // guest criticality
				new int[] {200,200}, // guest exectimes
				2, // hyper criticality
				1, // hyper exectime
				1);   // hyper priority

		MixedTrustTask mt2 = new MixedTrustTask(
				800, // period
				800, // deadline
				1, // guest criticality
				new int[] {200,500}, // guest exectimes
				2, // hyper criticality
				1, // hyper exectime
				0);   // hyper priority

		LayeredTrustZSRMScheduler sched = new LayeredTrustZSRMScheduler();
		sched.add(mt1);
		sched.add(mt2);

		System.out.println("Schedulable = "+sched.isSchedulable());

		for(MixedTrustTask t:sched.decreasingHypertaskPriority){
			System.out.println(t);
		}
	}

	public static void main(String args[]){
		testBasicTwoTasks();
	}
}
