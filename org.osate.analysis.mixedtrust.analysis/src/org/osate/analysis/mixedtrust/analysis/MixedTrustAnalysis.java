package org.osate.analysis.mixedtrust.analysis;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instance.SystemOperationMode;
import org.osate.aadl2.modelsupport.modeltraversal.SOMIterator;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustProperties;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustTask;
import org.osate.result.AnalysisResult;

import edu.cmu.sei.mtzsrm.LayeredTrustExactScheduler;

public final class MixedTrustAnalysis {
	public MixedTrustAnalysis() {
		super();
	}

	/*
	 * For now we just have an invoke() method that runs over all the SOMs in the model.
	 * That is all I need for the JUnit tests.
	 * Add more methods as they are needed rather than try to guess and just leave
	 * a bunch of useless junky methods in there.
	 */

	/**
	 * Analyze the given system instance, taking all the system operation modes into account.
	 *
	 * @param monitor The progress monitor to use, or {@code null} if one is not needed.
	 * @param systemInstance The system instance to analyze.
	 * @return The results in a {@code AnalysisResult} object.
	 */
	public AnalysisResult invoke(final IProgressMonitor monitor, final SystemInstance systemInstance) {
		final IProgressMonitor pm = monitor == null ? new NullProgressMonitor() : monitor;
		return analyzeBody(pm, systemInstance);
	}

	// TODO: Return AnalysisResult object
	private AnalysisResult analyzeBody(final IProgressMonitor monitor, final SystemInstance systemInstance) {
//		final AnalysisResult analysisResult = ResultUtil.createAnalysisResult("Bus  Load", systemInstance);
//		analysisResult.setResultType(ResultType.SUCCESS);
//		analysisResult.setMessage("Bus load analysis of " + systemInstance.getFullName());

		final SOMIterator soms = new SOMIterator(systemInstance);
		while (soms.hasNext()) {
			final SystemOperationMode som = soms.nextSOM();
//			final Result somResult = ResultUtil.createResult(
//					Aadl2Util.isPrintableSOMName(som) ? Aadl2Util.getPrintableSOMMembers(som) : "", som,
//					ResultType.SUCCESS);
//			analysisResult.getResults().add(somResult);

			scheduleMixedTrustTasks(systemInstance, som);
//			final BusLoadModel busLoadModel = BusLoadModelBuilder.buildModel(systemInstance, som);
//			analyzeBusLoadModel(busLoadModel, somResult, monitor);
		}
		monitor.done();

//		return analysisResult;

		// TODO: Return AnalysisResult object
		return null;
	}

	private void scheduleMixedTrustTasks(final SystemInstance systemInstance, final SystemOperationMode som) {
		final Optional<List<MixedTrustTask>> v = MixedTrustProperties.getMixedTrustTasks(systemInstance);
		if (v.isPresent()) {
			final LayeredTrustExactScheduler scheduler = new LayeredTrustExactScheduler();
			for (final MixedTrustTask mtt : v.get()) {
			}
		} else {
			// TODO: Report warning
		}
	}
}
