package org.osate.analysis.mixedtrust.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.contrib.deployment.DeploymentProperties;
import org.osate.aadl2.contrib.timing.TimingProperties;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instance.SystemOperationMode;
import org.osate.aadl2.modelsupport.modeltraversal.SOMIterator;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustBindings;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustTask;
import org.osate.result.AnalysisResult;
import org.osate.xtext.aadl2.properties.util.InstanceModelUtil;

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

//			scheduleMixedTrustTasks(systemInstance, som);
//			final BusLoadModel busLoadModel = BusLoadModelBuilder.buildModel(systemInstance, som);
//			analyzeBusLoadModel(busLoadModel, somResult, monitor);
		}
		monitor.done();

//		return analysisResult;

		// TODO: Return AnalysisResult object
		return null;
	}

//	private void scheduleMixedTrustTasks(final SystemInstance systemInstance, final SystemOperationMode som) {
//		final Optional<List<MixedTrustTask>> v = MixedTrustProperties.getMixedTrustTasks(systemInstance);
//		if (v.isPresent()) {
//			final LayeredTrustExactScheduler scheduler = new LayeredTrustExactScheduler();
//			for (final MixedTrustTask mtt : v.get()) {
//			}
//		} else {
//			// TODO: Report warning
//		}
//	}

	// ======================================================================
	// === Consistency Checking Methods
	// ======================================================================

	private void checkMixedTrustBindings(final ComponentInstance processor, final MixedTrustBindings mixedTrustBindings,
			final Domains domains) {
		final InstanceObject guestOS = mixedTrustBindings.getGuestos().orElse(null);
		final InstanceObject hyperVisor = mixedTrustBindings.getHypervisor().orElse(null);

		if (guestOS == null) {
			// error
		} else {
			if (!getProcessorBindings(guestOS).contains(processor)) {
				// error: not directly bound to processor
			}
		}

		if (hyperVisor == null) {
			// error
		} else {
			if (!getProcessorBindings(hyperVisor).contains(processor)) {
				// error: not directly bound to processor
			}
		}

		if (guestOS != null && hyperVisor != null) {
			/* Check that only guestOS and hyperVisor are bound to the processor */
			for (final ComponentInstance ci : getBoundVirtualProcessors(processor)) {
				if (ci != guestOS && ci != hyperVisor) {
					// error: extra things bound to the processor
				}
			}

			domains.addGuestOS(guestOS);
			domains.addHyperVisor(hyperVisor);
		}
	}

	private void checkMixedTrustTask(final MixedTrustTask mtt, final Domains domains) {
		if (mtt.getPeriod().isEmpty()) {
			// error
		}
		if (mtt.getDeadline().isEmpty()) {
			// error
		}

		final InstanceObject guestTask = mtt.getGuesttask().orElse(null);
		final InstanceObject hyperTask = mtt.getHypertask().orElse(null);
		final InstanceObject guestOsBinding = checkTask(guestTask, domains::isGuestOS);
		final InstanceObject hyperVisorBinding = checkTask(hyperTask, domains::isHyperVisor);
		if (guestOsBinding != null && hyperVisorBinding != null && guestOsBinding != hyperVisorBinding) {
			// error
		}
	}

	private InstanceObject checkTask(final InstanceObject task,
			final Function<InstanceObject, Boolean> checkTaskMembership) {
		if (task == null) {
			// error
			return null;
		} else {
			if (TimingProperties.getPeriod(task).isPresent()) {
				// warning
			}
			if (TimingProperties.getDeadline(task).isPresent()) {
				// warning
			}

			final List<InstanceObject> boundProcs = getProcessorBindings(task);
			if (boundProcs.isEmpty()) {
				// error
				return null;
			} else if (boundProcs.size() > 1) {
				// error
				return null;
			} else {
				final InstanceObject boundTo = boundProcs.get(0);
				if (!checkTaskMembership.apply(boundTo)) {
					// error
					return null;
				}
				return boundTo;
			}
		}
	}

	// ======================================================================

	// ======================================================================

	// ======================================================================
	// == These should be in InstanceModelUtil, the current bound processor
	// == methods are not quite right, but I don't want to deal with what
	// == fixing those methods might break right now,
	// == See Issue 2702
	// ======================================================================

	/**
	 * return set of components the specified instance object (thread, thread group, process, system, virtual processor, device) is directly bound to.
	 * Takes into account that virtual processors may be subcomponents of other virtual processors or processors.
	 * @param io An instance object for a thread, thread group, process, system, virtual processor, or device.
	 * @return A list of the processor or virtual processor instance objects that <code>io</code> is directly bound to.
	 */
	private static List<InstanceObject> getProcessorBindings(final InstanceObject io) {
		final List<InstanceObject> bindinglist = DeploymentProperties.getActualProcessorBinding(io)
				.orElse(Collections.emptyList());
		/**
		 * If we have a virtual processor, we consider that it is bound to
		 * its containing processor or virtual processor.
		 */
		if (bindinglist.isEmpty() && io instanceof ComponentInstance
				&& ((ComponentInstance) io).getCategory() == ComponentCategory.VIRTUAL_PROCESSOR) {
			final ComponentInstance parent = io.getContainingComponentInstance();
			if (parent.getCategory() == ComponentCategory.PROCESS
					|| parent.getCategory() == ComponentCategory.VIRTUAL_PROCESSOR) {
				return Collections.singletonList(parent);
			}
		}
		return bindinglist;
	}

	/**
	 * Test if {@code boundObject} is directly or indirectly bound to {@code processor}.
	 * It could be bound to a virtual processor which in turn is bound to a processor.
	 * the component instance can be a thread, process, or a virtual processor instance
	 * @param boundObject Am instance object representing a  thread, thread group, process, system, virtual processor, or device.
	 * @param processor A component instance representing a processor or virtual processor.
	 * @return {@code true} if and only if {@code boundObject} is bound to {@code processor}
	 */
	private static boolean isBoundToProcessor(InstanceObject boundObject, ComponentInstance processor) {
		final List<InstanceObject> bindinglist = getProcessorBindings(boundObject);
		for (final InstanceObject boundCompInstance : bindinglist) {
			if (InstanceModelUtil.isVirtualProcessor(boundCompInstance)) {
				// it is bound to or contained in
				if (isBoundToProcessor(boundCompInstance, processor)) {
					return true;
				}
			} else if (boundCompInstance == processor) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all virtual processors bound to the given processor or virtual processor.
	 * @param procOrVP A component instance representing a processor or virtual processor,
	 * @return A list of all the virtual processors directly or indirectly bound to {@code procOrVP}.
	 */
	public static EList<ComponentInstance> getBoundVirtualProcessors(final ComponentInstance procOrVP) {
		final SystemInstance root = procOrVP.getSystemInstance();
		final EList<ComponentInstance> virtualProcs = root
				.getAllComponentInstances(ComponentCategory.VIRTUAL_PROCESSOR);
		final EList<ComponentInstance> result = new BasicEList<ComponentInstance>();
		for (final ComponentInstance ci : virtualProcs) {
			if (isBoundToProcessor(ci, procOrVP)) {
				result.add(ci);
			}
		}
		return result;
	}

	// ======================================================================

	private static final class Domains {
		private final Set<InstanceObject> guestOSes = new HashSet<>();
		private final Set<InstanceObject> hyperVisors = new HashSet<>();

		public void addGuestOS(final InstanceObject guestOS) {
			guestOSes.add(guestOS);
		}

		public void addHyperVisor(final InstanceObject hyperVisor) {
			hyperVisors.add(hyperVisor);
		}

		public boolean isGuestOS(final InstanceObject task) {
			return guestOSes.contains(task);
		}

		public boolean isHyperVisor(final InstanceObject task) {
			return hyperVisors.contains(task);
		}
	}
}

