package edu.cmu.sei.mtzsrm.experiments;
import edu.cmu.sei.mtzsrm.LayeredTrustExactScheduler;
import edu.cmu.sei.mtzsrm.LayeredTrustScheduler;
import edu.cmu.sei.mtzsrm.MixedTrustTask;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Experimenter {

	public static MixedTrustTask generateTask(
				double utilization,
				int [] periodRange,
				double hyperGuestRatio,
				double DTRatio,
				Random random
			){
		MixedTrustTask task=null;
		
		int T,C,kC,CkC,D;
		
		T = (int) (periodRange[0] + (random.nextDouble() * (periodRange[1]-periodRange[0])));
		CkC = (int) (utilization * T);
		C = (int) (CkC * (1-hyperGuestRatio));
		kC = (int) (CkC * hyperGuestRatio);
		D = (int) (T * DTRatio);
		
		task = new MixedTrustTask(
				T, // period
				D, // deadline
				0, // guest criticality
				new int[] {C}, // guest exectimes
				1, // hyper criticality
				kC, // hyper exectime
				0);   // priority

		return task; 
	}
	
	public static void generateTaskset(
			LayeredTrustScheduler scheduler,
			double utilization,
			int numTasks,
			double tmaxtminRatio,
			double DTRatio,
			double hyperGuestRatio,
			Random random
			){
		
		double utilPerTask = utilization / ((double)numTasks);
		int Tmin = 1000;
		int Tmax = (int) (Tmin * tmaxtminRatio);
		
		for (int i=0;i<numTasks;i++){
			MixedTrustTask task = generateTask(utilPerTask,
												new int []{Tmin,Tmax},
												hyperGuestRatio,
												DTRatio,
												random
												);
			scheduler.add(task);
		}
		scheduler.assignDeadlineMonotonicPriorities();
	}
		
	final static int NUM_EXPERIMENTS =100000;
	
	public static void experimentGrowingUtilization(Random random, int numTasks, double tmaxTminRatio, double dTRatio, double hyperGuestRatio){
		// increasing utilization
		HashMap<Double, ExperimentResult> incUtilPlot = new HashMap<Double,ExperimentResult>();
		System.out.println("------ Growing Utilization -------");
		for (double util = 0.1 ; util <= 1.0 ; util += 0.1){
			long startms,endms;
			ExperimentResult res = new ExperimentResult();
			
			for (int x = 0; x<NUM_EXPERIMENTS;x++){
				LayeredTrustExactScheduler sched = new LayeredTrustExactScheduler();
				generateTaskset(sched, 
						util, 		// utilization 
						numTasks, 	// numTasks
						tmaxTminRatio,   	// tmaxtminRatio
						dTRatio,  		// DTratio
						hyperGuestRatio,		// hyperGuestRatio
						random);
				startms = System.currentTimeMillis();
				res.avgSchedulable += sched.isSchedulable() ? 1 :0;
				endms = System.currentTimeMillis();
				res.avgDuration += endms-startms;
			}
			System.out.println(res);
			res.avgSchedulable = res.avgSchedulable / ((double)NUM_EXPERIMENTS);
			res.avgDuration = res.avgDuration / ((double)NUM_EXPERIMENTS);
			incUtilPlot.put(util,res);			
		}
		System.out.println("----------\n\n");
		
		try{
			FileWriter writer1 = new FileWriter("incUtilSched.csv", false);
			FileWriter writer2 = new FileWriter("incUtilTime.csv", false);
			writer1.write("Utilization, Schedulable\n");
			writer2.write("Utilization, Duration\n");
			for (Map.Entry<Double, ExperimentResult> entry:incUtilPlot.entrySet()){
				writer1.write(entry.getKey()+","+entry.getValue().avgSchedulable+"\n");
				writer2.write(entry.getKey()+","+entry.getValue().avgDuration+"\n");
			}
			writer1.close();
			writer2.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void experimentGrowingNumberOfTasks(Random random, double utilization, double tmaxTminRatio, double dTRatio, double hyperGuestRatio){
		boolean schedulable;
		HashMap<Integer, ExperimentResult> incNumTaskPlot = new HashMap<Integer,ExperimentResult>();
		System.out.println("------ Growing Number of Tasks -------");
		for (int i=3 ; i<= 200 ; i++){
			long startms,endms;
			ExperimentResult res = new ExperimentResult();

			if (i==100){
				//Logger.enable();
			}
			
			for (int x = 0; x<NUM_EXPERIMENTS;x++){			
				LayeredTrustExactScheduler sched = new LayeredTrustExactScheduler();
				generateTaskset(sched, 
						utilization, 	// utilization 
						i,   	// numTasks
						tmaxTminRatio,   // tmaxtminRatio
						dTRatio,  	// DTratio
						hyperGuestRatio,	// hyperGuestRatio
						random);
				startms = System.currentTimeMillis();
				schedulable = sched.isSchedulable();
				res.avgSchedulable += schedulable ? 1: 0; //sched.isSchedulable() ? 1 :0;
//				if (!schedulable){
//					System.out.println("Not schedulable!\n");
//				}
				endms = System.currentTimeMillis();
				res.avgDuration += endms-startms;
			}
			System.out.println(res);
			res.avgSchedulable = res.avgSchedulable / ((double)NUM_EXPERIMENTS);
			res.avgDuration = res.avgDuration / ((double)NUM_EXPERIMENTS);
			incNumTaskPlot.put(i, res);
		}
		
		System.out.println("----------\n\n");

		try{
			FileWriter writer1 = new FileWriter("incNumTasksSched.csv", false);
			FileWriter writer2 = new FileWriter("incNumTasksTime.csv", false);
			writer1.write("Num Tasks, Schedulable Ratio\n");
			writer2.write("Num Tasks, Duration\n");
			for (Map.Entry<Integer, ExperimentResult> entry:incNumTaskPlot.entrySet()){
				writer1.write(entry.getKey()+","+entry.getValue().avgSchedulable+"\n");
				writer2.write(entry.getKey()+","+entry.getValue().avgDuration+"\n");
			}
			writer1.close();
			writer2.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void experimentGrowingKCC(Random random, double utilization, int numTasks, double tmaxTminRatio, double dTRatio){
		HashMap<Double,ExperimentResult> incKCCPlot = new HashMap<Double,ExperimentResult>();
		System.out.println("------ Growing kC/(C+kC) -------");
		for (double kCCr=0.1 ; kCCr <= 1.0; kCCr += 0.05){
			long startms,endms;
			ExperimentResult res = new ExperimentResult();
			
			for (int x = 0; x<NUM_EXPERIMENTS;x++){						
				LayeredTrustExactScheduler sched = new LayeredTrustExactScheduler();
				generateTaskset(sched, 
						utilization, 	// utilization 
						numTasks,   	// numTasks
						tmaxTminRatio,  		// tmaxtminRatio
						dTRatio,  			// DTratio
						kCCr,			// hyperGuestRatio
						random);
				startms = System.currentTimeMillis();
				res.avgSchedulable += sched.isSchedulable() ? 1 :0;
				endms = System.currentTimeMillis();
				res.avgDuration += endms-startms;
			}
			System.out.println(res);
			res.avgSchedulable = res.avgSchedulable / ((double)NUM_EXPERIMENTS);
			res.avgDuration = res.avgDuration / ((double)NUM_EXPERIMENTS);
			incKCCPlot.put(kCCr, res);			
		}

		System.out.println("----------\n\n");

		try{
			FileWriter writer1 = new FileWriter("incKCCSched.csv", false);
			FileWriter writer2 = new FileWriter("incKCCTime.csv", false);
			writer1.write("kC /C+kC, Schedulable Ratio\n");
			writer2.write("kC / C+kC, Duration\n");
			for (Map.Entry<Double, ExperimentResult> entry:incKCCPlot.entrySet()){
				writer1.write(entry.getKey()+","+entry.getValue().avgSchedulable+"\n");
				writer2.write(entry.getKey()+","+entry.getValue().avgDuration+"\n");
			}
			writer1.close();
			writer2.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void experimentGrowingTmaxTmin(Random random, double utilization, int numTasks, double dTRatio, double hyperGuestRatio){
		HashMap<Double,ExperimentResult> inctmaxtminPlot = new HashMap<Double,ExperimentResult>();
		System.out.println("------ Growing Tmax/Tmin -------");		
		for (double tmaxtmin = 1.0 ; tmaxtmin <= 1024.0 ; tmaxtmin *= 2.0){//tmaxtmin <= 1000000.0 ; tmaxtmin *= 10.0){
			long startms,endms;
			ExperimentResult res = new ExperimentResult();

			for (int x = 0; x<NUM_EXPERIMENTS;x++){	
				boolean schedulable;
				LayeredTrustExactScheduler sched = new LayeredTrustExactScheduler();
				generateTaskset(sched, 
						utilization, 		// utilization 
						numTasks,   		// numTasks
						tmaxtmin,   		// tmaxtminRatio
						dTRatio,  			// DTratio
						hyperGuestRatio,	// hyperGuestRatio
						random);
				startms = System.currentTimeMillis();
				schedulable = sched.isSchedulable();
				res.avgSchedulable += schedulable ? 1: 0; //sched.isSchedulable() ? 1 :0;
				if (schedulable){
					if (tmaxtmin == 2.0){
						//Logger.addSuccess(sched.getTaskset());
					}
				}
				endms = System.currentTimeMillis();
				res.avgDuration += endms-startms;
			}
			System.out.println(res);
			res.avgSchedulable = res.avgSchedulable / ((double)NUM_EXPERIMENTS);
			res.avgDuration = res.avgDuration / ((double)NUM_EXPERIMENTS);
			inctmaxtminPlot.put(tmaxtmin, res);
		}
		
		System.out.println("----------\n\n");

		try{
			FileWriter writer1 = new FileWriter("incTmaxTminSched.csv", false);
			FileWriter writer2 = new FileWriter("incTmaxTminTime.csv", false);
			writer1.write("Tmax/Tmin, Schedulable Ratio\n");
			writer2.write("Tmax/Tmin, Duration\n");
			for (Map.Entry<Double, ExperimentResult> entry:inctmaxtminPlot.entrySet()){
				writer1.write(entry.getKey()+","+entry.getValue().avgSchedulable+"\n");
				writer2.write(entry.getKey()+","+entry.getValue().avgDuration+"\n");
			}
			writer1.close();
			writer2.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void experimentGrowingDT(Random random, double utilization, int numTasks, double tmaxTminRatio, double hyperGuestRatio){
		HashMap<Double,ExperimentResult> incdtPlot = new HashMap<Double,ExperimentResult>();		
		System.out.println("------ Growing D/T -------");
		for (double dt= 0.1;dt<=1.0 ; dt += 0.1){
			long startms,endms;
			ExperimentResult res = new ExperimentResult();

			for (int x = 0; x<NUM_EXPERIMENTS;x++){						
				LayeredTrustExactScheduler sched = new LayeredTrustExactScheduler();
				generateTaskset(sched, 
						utilization, 		// utilization 
						numTasks,   		// numTasks
						tmaxTminRatio,   	// tmaxtminRatio
						dt,  				// DTratio
						hyperGuestRatio,	// hyperGuestRatio
						random);
				startms = System.currentTimeMillis();
				res.avgSchedulable += (sched.isSchedulable() ? 1 :0);
				endms = System.currentTimeMillis();
				res.avgDuration += endms-startms;
			}
			System.out.println(res);
			res.avgSchedulable = res.avgSchedulable / ((double)NUM_EXPERIMENTS);
			res.avgDuration = res.avgDuration / ((double)NUM_EXPERIMENTS);
			incdtPlot.put(dt, res);
		}
		
		System.out.println("----------\n\n");

		try{
			FileWriter writer1 = new FileWriter("incDTSched.csv", false);
			FileWriter writer2 = new FileWriter("incDTTime.csv", false);
			writer1.write("D/T, Schedulable Ratio\n");
			writer2.write("D/T, Duration\n");
			for (Map.Entry<Double, ExperimentResult> entry:incdtPlot.entrySet()){
				writer1.write(entry.getKey()+","+entry.getValue().avgSchedulable+"\n");
				writer2.write(entry.getKey()+","+entry.getValue().avgDuration+"\n");
			}
			writer1.close();
			writer2.close();
		} catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void main(String args[]){
		Random random = new Random();
		double utilization=0.8;
		double TmaxTminRatio = 100.0;
		double DTRatio = 1.0 ; 
		double hyperGuestRatio = 0.1;
		
		//Logger.clear();
		//Logger.setMaximumNumberRecords(1000);
		//Logger.disable();
		
		/**
		 *  Number of Task rationale: At 100 tasks the schedulability is as low as ~30% so
		 *  we select a smaller number.
		 */
		int numTasks=10;
		Thread t1 = new Thread(new Runnable(){
			public void run() {
				experimentGrowingUtilization(random, numTasks,TmaxTminRatio,DTRatio,hyperGuestRatio);				
			}
			
		});
		t1.start();		
		//experimentGrowingUtilization(random, numTasks,TmaxTminRatio,DTRatio,hyperGuestRatio);
		
		Thread t2 = new Thread(new Runnable(){
			public void run() {
				experimentGrowingNumberOfTasks(random,utilization,TmaxTminRatio,DTRatio,hyperGuestRatio);
			}
		});
		t2.start();
		//experimentGrowingNumberOfTasks(random,utilization,TmaxTminRatio,DTRatio,hyperGuestRatio);
		
		Thread t3 = new Thread(new Runnable(){
			public void run() {
				experimentGrowingKCC(random,utilization, numTasks,TmaxTminRatio,DTRatio);
			}
		});
		t3.start();
		//experimentGrowingKCC(random,utilization, numTasks,TmaxTminRatio,DTRatio);
		
		Thread t4 = new Thread(new Runnable(){
			public void run() {
				experimentGrowingTmaxTmin(random,utilization, numTasks, DTRatio, hyperGuestRatio);
			}
		});
		t4.start();		
		//experimentGrowingTmaxTmin(random,utilization, numTasks, DTRatio, hyperGuestRatio);
		
		Thread t5 = new Thread(new Runnable(){
			public void run() {
				experimentGrowingDT(random,utilization, numTasks,TmaxTminRatio,hyperGuestRatio);
			}
		});
		t5.start();			
		//experimentGrowingDT(random,utilization, numTasks,TmaxTminRatio,hyperGuestRatio);
		
		System.out.println("Waiting");
		
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done");
		//Logger.writeLog("failures.csv");
	}
}
