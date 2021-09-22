package org.osate.analysis.mixedtrust.analysis.ui.handlers;

import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.modelsupport.errorreporting.MarkerAnalysisErrorReporter;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.analysis.mixedtrust.analysis.MixedTrustAnalysis;
import org.osate.result.AnalysisResult;
import org.osate.result.Result;
import org.osate.result.util.ResultUtil;
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
				new ResultWriter(outputFile).writeAnalysisResults(analysisResult, subMonitor.split(1));
//				writeCSVFile(analysisResult, outputFile, subMonitor.split(1));
			} catch (final OperationCanceledException e) {
				cancelled = true;
			}

			return cancelled ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

	// ============================================================
	// == XXX: Should move this to the superclass, or otherwise
	// == abstract it somehow
	// ============================================================

	// === CSV Output methods ===

	private static final class ResultWriter extends CSVAnalysisResultWriter {
		protected ResultWriter(final IFile outputFile) {
			super(outputFile);
		}

		@Override
		protected void generateContentforSOM(final PrintWriter pw, final Result somResult,
				final IProgressMonitor monitor) {
			final SubMonitor subMonitor = SubMonitor.convert(monitor, 2);

			/*
			 * Output the diagnostics (only at the SOM level)
			 */
			if (!somResult.getDiagnostics().isEmpty()) {
				generateContentforDiagnostics(pw, somResult.getDiagnostics(), subMonitor.split(1));
				pw.println();
			}

			/*
			 * Output results for each processor
			 */
			final SubMonitor loopProgress = subMonitor.split(1).setWorkRemaining(somResult.getSubResults().size());
			somResult.getSubResults().forEach(pr -> generateContentforProcessor(pw, pr, loopProgress.split(1)));
			pw.println();
		}

		private void generateContentforProcessor(final PrintWriter pw, final Result processorResult,
				final IProgressMonitor monitor) {
			printItem(pw, String.format("Mixed trust tasks on processor %s %s schedulable",
					processorResult.getMessage(), ResultUtil.getBoolean(processorResult, 0) ? "are" : "are not"));
			pw.println();

			final int size = processorResult.getSubResults().size();
			final SubMonitor subMonitor = SubMonitor.convert(monitor, size);
			if (size > 0) {
				printItems(pw, "Mixed Task Name", "Guest Task Thread", "Hyper Task Thread", "E");
				processorResult.getSubResults().forEach(tr -> generateContentforTask(pw, tr, subMonitor.split(1)));
			}
			pw.println();
		}

		private void generateContentforTask(final PrintWriter pw, final Result taskResult,
				final IProgressMonitor monitor) {
			final SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
			printItems(pw, taskResult.getMessage(), ResultUtil.getString(taskResult, 1),
					ResultUtil.getString(taskResult, 2),
					String.format("%d microseconds", ResultUtil.getInteger(taskResult, 0)));
			subMonitor.split(1);
		}
	}

//	private static void writeCSVFile(final AnalysisResult analysisResult, final IFile outputFile,
//			final IProgressMonitor monitor) {
//		final String csvContent = getCSVasString(analysisResult);
//		final InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
//
//		try {
//			if (outputFile.exists()) {
//				outputFile.setContents(inputStream, true, true, monitor);
//			} else {
//				outputFile.create(inputStream, true, monitor);
//			}
//		} catch (final CoreException e) {
//			Activator.logThrowable(e);
//		}
//	}
//
//	private static String getCSVasString(final AnalysisResult analysisResult) {
//		final StringWriter writer = new StringWriter();
//		final PrintWriter pw = new PrintWriter(writer);
//		generateCSVforAnalysis(pw, analysisResult);
//		pw.close();
//		return writer.toString();
//	}
//
//	private static void generateCSVforAnalysis(final PrintWriter pw, final AnalysisResult analysisResult) {
//		pw.println(analysisResult.getMessage());
//		pw.println();
//		pw.println();
//		analysisResult.getResults().forEach(somResult -> generateCSVforSOM(pw, somResult));
//	}
//
//	private static void generateCSVforSOM(final PrintWriter pw, final Result somResult) {
//		if (Aadl2Util.isPrintableSOMName((SystemOperationMode) somResult.getModelElement())) {
//			printItem(pw, "Analysis results in modes " + somResult.getMessage());
//			pw.println();
//		}
//
//		/*
//		 * Output the diagnostics (only at the SOM level)
//		 */
//		if (!somResult.getDiagnostics().isEmpty()) {
//			generateCSVforDiagnostics(pw, somResult.getDiagnostics());
//			pw.println();
//		}
//
//		/*
//		 * Output results for each processor
//		 */
//		somResult.getSubResults().forEach(pr -> generateCSVforProcessor(pw, pr));
//		pw.println();
//	}
//
//	private static void generateCSVforProcessor(final PrintWriter pw, final Result processorResult) {
//		printItem(pw, String.format("Mixed trust tasks on processor %s %s schedulable", processorResult.getMessage(),
//				ResultUtil.getBoolean(processorResult, 0) ? "are" : "are not"));
//		pw.println();
//
//		processorResult.getSubResults().forEach(tr -> generateCSVforTask(pw, tr));
//		pw.println();
//	}
//
//	private static void generateCSVforTask(final PrintWriter pw, final Result taskResult) {
//		printItems(pw, taskResult.getMessage(), ResultUtil.getString(taskResult, 1),
//				ResultUtil.getString(taskResult, 2),
//				String.format("%d microseconds", ResultUtil.getInteger(taskResult, 0)));
//	}
//
//	// ==== Low-level CSV format, this should be abstracted somewhere
//
//	private static void generateCSVforDiagnostics(final PrintWriter pw, final List<Diagnostic> diagnostics) {
//		for (final Diagnostic issue : diagnostics) {
//			printItem(pw, issue.getDiagnosticType().getName() + ": " + issue.getMessage());
//			pw.println();
//		}
//	}
//
//	private static void printItems(final PrintWriter pw, final String item1, final String... items) {
//		printItem(pw, item1);
//		for (final String nextItem : items) {
//			printSeparator(pw);
//			printItem(pw, nextItem);
//		}
//		pw.println();
//	}
//
//	private static void printItem(final PrintWriter pw, final String item) {
//		// TODO: Doesn't handle quotes in the item!
//		pw.print('"');
//		pw.print(item);
//		pw.print('"');
//	}
//
//	private static void printSeparator(final PrintWriter pw) {
//		pw.print(",");
//	}
}
