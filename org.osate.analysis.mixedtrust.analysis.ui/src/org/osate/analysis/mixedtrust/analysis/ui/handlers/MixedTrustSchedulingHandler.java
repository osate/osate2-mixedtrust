package org.osate.analysis.mixedtrust.analysis.ui.handlers;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.osate.aadl2.Element;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.modelsupport.errorreporting.MarkerAnalysisErrorReporter;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.analysis.mixedtrust.analysis.MixedTrustAnalysis;
import org.osate.result.AnalysisResult;
import org.osate.result.Diagnostic;
import org.osate.result.Result;
import org.osate.ui.handlers.AbstractAnalysisHandler;

public final class MixedTrustSchedulingHandler extends AbstractAnalysisHandler {
	private static final String MARKER_TYPE = "org.osate.analysis.mixedtrust.analysis.ui.MixedTrustSchedulingAnalysisMarker";
	private static final String OUTPUT_FILE_SUFFIX = "__MixedTrust.csv";
	private static final String REPORT_SUB_DIR = "MixedTrust";

	public MixedTrustSchedulingHandler() {
		super();
	}

	@Override
	protected String getSubDirName() {
		return REPORT_SUB_DIR;
	}

	@Override
	protected String getOutputFileForAaxlFile(final IFile aaxlFile, final String filename) {
		return filename + OUTPUT_FILE_SUFFIX;
	}


	@Override
	protected Job createAnalysisJob(final IFile aaxlFile, final IFile outputFile) {
		return new MixedTrustJob(aaxlFile, outputFile);
	}

	private final class MixedTrustJob extends WorkspaceJob {
		private final IFile aaxlFile;
		private final IFile outputFile;

		public MixedTrustJob(final IFile aaxlFile, final IFile outputFile) {
			super("Mixed trust scheduling analysis of " + aaxlFile.getName());
			this.aaxlFile = aaxlFile;
			this.outputFile = outputFile;
		}

		@Override
		public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
			final AnalysisErrorReporterManager errManager = new AnalysisErrorReporterManager(
					new MarkerAnalysisErrorReporter.Factory(MARKER_TYPE));

			// Three phases (1) analysis, (2) marker generation, (3) csv file
			final SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
			boolean cancelled = false;

			try {
				final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(subMonitor.split(1),
						(SystemInstance) AadlUtil.getElement(aaxlFile));
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				generateMarkers(analysisResult, errManager);
				subMonitor.worked(1);
//				writeCSVFile(analysisResult, outputFile, subMonitor.split(1));
			} catch (final OperationCanceledException e) {
				cancelled = true;
			}

			return cancelled ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

	// ============================================================
	// == XXX: Should move this to the superclass?
	// ============================================================

	private static void generateMarkers(final AnalysisResult analysisResult,
			final AnalysisErrorReporterManager errManager) {
		// Handle each SOM
		analysisResult.getResults().forEach(r -> {
			final String somName = r.getMessage();
			final String somPostfix = somName.isEmpty() ? "" : (" in modes " + somName);
			generateMarkersForSOM(r, errManager, somPostfix);
		});
	}

	private static void generateMarkersForSOM(final Result result, final AnalysisErrorReporterManager errManager,
			final String somPostfix) {
		generateMarkersFromDiagnostics(result.getDiagnostics(), errManager, somPostfix);
		result.getSubResults().forEach(r -> generateMarkersForSOM(r, errManager, somPostfix));
	}

	private static void generateMarkersFromDiagnostics(final List<Diagnostic> diagnostics,
			final AnalysisErrorReporterManager errManager, final String somPostfix) {
		diagnostics.forEach(issue -> {
			switch (issue.getDiagnosticType()) {
			case ERROR:
				errManager.error((Element) issue.getModelElement(), issue.getMessage() + somPostfix);
				break;
			case INFO:
				errManager.info((Element) issue.getModelElement(), issue.getMessage() + somPostfix);
				break;
			case WARNING:
				errManager.warning((Element) issue.getModelElement(), issue.getMessage() + somPostfix);
				break;
			default:
				// Do nothing.
			}
		});
	}
}
