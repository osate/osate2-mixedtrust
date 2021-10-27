/**
 * Mixed-Trust Scheduling Analysis OSATE Plugin
 *
 * Copyright 2021 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
 * AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR
 * PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF
 * THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF
 * ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT
 * INFRINGEMENT.
 *
 * Released under the Eclipse Public License - v 2.0 license, please see
 * license.txt or contact permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public
 * release and unlimited distribution.  Please see Copyright notice for
 * non-US Government use and distribution.
 *
 * Carnegie Mellon® is registered in the U.S. Patent and Trademark Office
 * by Carnegie Mellon University.
 *
 * DM21-0927
 */

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
		private static final String JOB_NAME_PREFIX = "Mixed trust scheduling analysis of ";

		private final IFile aaxlFile;
		private final IFile outputFile;

		public MixedTrustJob(final IFile aaxlFile, final IFile outputFile) {
			super(JOB_NAME_PREFIX + aaxlFile.getName());
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
			} catch (final OperationCanceledException e) {
				cancelled = true;
			}

			return cancelled ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

	}

	// === CSV Output methods ===

	private static final class ResultWriter extends CSVAnalysisResultWriter {
		private static final String PROCESSOR_HEADER_FORMAT = "Mixed trust tasks on processor %s %s schedulable";
		private static final String ARE = "are";
		private static final String ARE_NOT = "are not";

		private static final String MIXED_TASK_NAME_HEADER = "Mixed Task Name";
		private static final String GUEST_TASK_THREAD_HEADER = "Guest Task Thread";
		private static final String HYPER_TASK_THREAD_HEADER = "Hyper Task Thread";
		private static final String E_HEADER = "E";
		private static final String E_VALUE_FORMAT = "%d microseconds";

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
			printItem(pw, String.format(PROCESSOR_HEADER_FORMAT,
					processorResult.getMessage(), ResultUtil.getBoolean(processorResult, 0) ? ARE : ARE_NOT));
			pw.println();

			final int size = processorResult.getSubResults().size();
			final SubMonitor subMonitor = SubMonitor.convert(monitor, size);
			if (size > 0) {
				printItems(pw, MIXED_TASK_NAME_HEADER, GUEST_TASK_THREAD_HEADER, HYPER_TASK_THREAD_HEADER, E_HEADER);
				processorResult.getSubResults().forEach(tr -> generateContentforTask(pw, tr, subMonitor.split(1)));
			}
			pw.println();
		}

		private void generateContentforTask(final PrintWriter pw, final Result taskResult,
				final IProgressMonitor monitor) {
			final SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
			printItems(pw, taskResult.getMessage(), ResultUtil.getString(taskResult, 1),
					ResultUtil.getString(taskResult, 2),
					String.format(E_VALUE_FORMAT, ResultUtil.getInteger(taskResult, 0)));
			subMonitor.split(1);
		}
	}
}
