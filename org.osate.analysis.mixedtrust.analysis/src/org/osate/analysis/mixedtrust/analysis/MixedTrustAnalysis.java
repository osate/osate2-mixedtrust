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
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.contrib.deployment.DeploymentProperties;
import org.osate.aadl2.contrib.timing.TimingProperties;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instance.SystemOperationMode;
import org.osate.aadl2.modelsupport.modeltraversal.SOMIterator;
import org.osate.aadl2.util.Aadl2Util;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustBindings;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustProperties;
import org.osate.analysis.mixedtrust.contribution.mixedtrustproperties.MixedTrustTask;
import org.osate.result.AnalysisResult;
import org.osate.result.DiagnosticType;
import org.osate.result.Result;
import org.osate.result.ResultType;
import org.osate.result.util.ResultUtil;
import org.osate.xtext.aadl2.properties.util.InstanceModelUtil;

/**
 * Class for performing mixed trust scheduling analysis on a system.
 *
 * <p>The format for the returned {@code AnalysisResult} object is described below.  In this case, the diagnostics
 * are attached to the {@code Result} object for the system operation mode.  We do this because if the necessary property
 * associations are inconsistent then we cannot build a sensible model for analysis, and there won't be any results
 * below the SOM node.
 *
 * <p>For the {@code AnalysisResult} object itself:
 * <ul>
 *   <li>analysis = "Mixed Trust Scheduling"
 *   <li>modelElement = {@code SystemInstance} being analyzed
 *   <li>resultType = SUCCESS
 *   <li>message = "Mixed Trust Scheduling of ..."
 *   <li>diagnostics = empty list
 *   <li>parameters = empty list
 *   <li>results = one {@code Result} for each system operation mode
 *     <ul>
 *       <li>modelElement = {@code SystemOperationMode} instance object
 *       <li>resultType = SUCCESS
 *       <li>message = "" if the SOM is {@code null} or the empty SOM, otherwise "(xxx, ..., yyy)"
 *       <li>values = empty list
 *       <li>diagnostics = Diagnostics for the state of the system in the system operation mode.
 *       <li>subResults = one {@code Result} for each {@code ComponentInstance} with category of {@code Processor} that
 *       has a consistent property association for {@code Mixed_Trust_Processor}:
 *         <ul>
 *           <li>modelElement = {@code ComponentInstance} instance object
 *           <li>resultType = SUCCESS
 *           <li>message = The component's name from {@link ComponentInstance#getName()}
 *           <li>values[0] = TRUE or FALSE indicating whether the processor's tasks are schedulable (BooleanValue)
 *           <li>diagnostics = empty list
 *           <li>subResults = one {@Result} for each thread bound to a hyper visor or guest os running on this processor
 *             <ul>
 *               <li>modelElement = {@code ComponentInstance} instance object for the thread
 *               <li>resultType = SUCCESS
 *               <li>message = The component's name from {@link ComponentInstance#getName()}
 *               <li>values[0] = The priority value for the task (IntegerValue)
 *               <li>diagnostics = empty list
 *               <li>subResults = empty list
 *             </ul>
 *         </ul>
 *     </ul>
 * </ul>
 *
 * @since 4.0
 */
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
		final AnalysisResult analysisResult = ResultUtil.createAnalysisResult("Mixed Trust Scheduling", systemInstance);
		analysisResult.setResultType(ResultType.SUCCESS);
		analysisResult.setMessage("Mixed Trust Scheduling of " + systemInstance.getFullName());

		final SOMIterator soms = new SOMIterator(systemInstance);
		while (soms.hasNext()) {
			final SystemOperationMode som = soms.nextSOM();
			final Result somResult = ResultUtil.createResult(
					Aadl2Util.isPrintableSOMName(som) ? Aadl2Util.getPrintableSOMMembers(som) : "", som,
					ResultType.SUCCESS);
			analysisResult.getResults().add(somResult);

			/* Check the Mixed Trust Processor properties for correctness */
			final Domains domains = new Domains();
			for (final ComponentInstance processor : systemInstance
					.getAllComponentInstances(ComponentCategory.PROCESSOR)) {
				/*
				 * NB. getAllComponentInstances() claims to be sensitive to the current system operation mode, but
				 * this isn't true any more. Need to check if the processor is part of the current som.
				 */
				if (processor.isActive(som)) {
					MixedTrustProperties.getMixedTrustProcessor(processor).map(mixedTrustBindings -> {
						final EObject where = MixedTrustProperties.getMixedTrustProcessor_EObject(processor);
						checkMixedTrustBindings(somResult, where, processor, mixedTrustBindings, domains);
						return null;
					});
				}
			}

			/* Check the MixedTrustTasks properties for correctness */
			for (final ComponentInstance system : systemInstance.getAllComponentInstances(ComponentCategory.SYSTEM)) {
				if (system.isActive(som)) {
					MixedTrustProperties.getMixedTrustTasks(system).map(listOfTasks -> {
						final var iter = ((ListValue) MixedTrustProperties.getMixedTrustTasks_EObject(system))
								.getOwnedListElements()
								.iterator();
						for (final MixedTrustTask mixedTrustTask : listOfTasks) {
							checkMixedTrustTask(somResult, iter.next(), mixedTrustTask, domains);
						}
						return null;
					});
				}
			}

			// TODO: Errors (but not warnings) prevent scheduling analysis

			// TODO: Check monitor for being cancelled

			// TODO: Modes and property look up-- check that referenced components exist

//			/* Run scheduling for each mixed trust processor */
//			for (final ComponentInstance system : systemInstance.getAllComponentInstances(ComponentCategory.SYSTEM)) {
//				if (system.isActive(som)) {
//					MixedTrustProperties.getMixedTrustTasks(system).map(listOfTasks -> {
//						for (final MixedTrustTask mixedTrustTask : listOfTasks) {
////							scheduleTasks(mixedTrustTask, domains);
//						}
//						return null;
//					});
//				}
//			}
		}
		monitor.done();

		return analysisResult;
	}

	// ======================================================================
	// === Consistency Checking Methods
	// ======================================================================

	private void checkMixedTrustBindings(final Result result, final EObject where, final ComponentInstance processor,
			final MixedTrustBindings mixedTrustBindings, final Domains domains) {
		final InstanceObject guestOS = mixedTrustBindings.getGuestos().orElse(null);
		final InstanceObject hyperVisor = mixedTrustBindings.getHypervisor().orElse(null);

		if (guestOS == null) {
			error(result, where, "Mixed_Trust_Bindings must specifiy a value for field GuestOS");
		} else {
			if (!getProcessorBindings(guestOS).contains(processor)) {
				// error: not directly bound to processor
				error(result, where, "Virtual processor referenced by field GuestOS is not bound to processor "
						+ processor.getName());
			}
		}

		if (hyperVisor == null) {
			error(result, where, "Mixed_Trust_Bindings must specifiy a value for field HyperVisor");
		} else {
			if (!getProcessorBindings(hyperVisor).contains(processor)) {
				// error: not directly bound to processor
				error(result, where, "Virtual processor referenced by field HyperVisor is not bound to processor "
						+ processor.getName());
			}
		}

		if (guestOS != null && hyperVisor != null) {
			/* Check that only guestOS and hyperVisor are bound to the processor */
			for (final ComponentInstance ci : getBoundVirtualProcessors(processor)) {
				if (ci != guestOS && ci != hyperVisor) {
					error(result, processor, "Component " + ci.getName()
							+ " is not a GuestOS or HyperVisor but is bound to processor " + processor.getName());
				}
			}

			domains.addGuestOS(guestOS);
			domains.addHyperVisor(hyperVisor);
		}
	}

	private void checkMixedTrustTask(final Result result, final EObject where, final MixedTrustTask mtt,
			final Domains domains) {
		if (mtt.getPeriod().isEmpty()) {
			error(result, where, "Mixed_Trust_Task must specify a value for field Period");
		}
		if (mtt.getDeadline().isEmpty()) {
			error(result, where, "Mixed_Trust_Task must specify a value for field Deadline");
		}

		final InstanceObject guestTask = mtt.getGuesttask().orElse(null);
		final InstanceObject hyperTask = mtt.getHypertask().orElse(null);
		final InstanceObject guestOsBinding = checkTask(result, where, guestTask, domains::isGuestOS,
				"GuestOS");
		final InstanceObject hyperVisorBinding = checkTask(result, where, hyperTask, domains::isHyperVisor,
				"HyperVisor");
		if (guestOsBinding != null && hyperVisorBinding != null && guestOsBinding != hyperVisorBinding) {
			error(result, where,
					"GuestOS and HyperVisor are bound to different processors");
		}
	}

	private InstanceObject checkTask(final Result result, final EObject where, final InstanceObject task,
			final Function<InstanceObject, Boolean> checkTaskMembership, String fieldName) {
		if (task == null) {
			error(result, where, "Mixed_Trust_Task must specify a value for field " + fieldName);
			return null;
		} else {
			if (TimingProperties.getPeriod(task).isPresent()) {
				warning(result, where, "Referenced thread " + task.getName() + " specifies a Period value");
			}
			if (TimingProperties.getDeadline(task).isPresent()) {
				warning(result, where, "Referenced thread " + task.getName() + " specifies a Deadline value");
			}

			final List<InstanceObject> boundProcs = getProcessorBindings(task);
			if (boundProcs.isEmpty()) {
				error(result, where, "Referenced thread " + task.getName() + " is not bound");
				return null;
			} else if (boundProcs.size() > 1) {
				error(result, where, "Referenced thread " + task.getName() + " bound to more than one component");
				return null;
			} else {
				final InstanceObject boundTo = boundProcs.get(0);
				if (!checkTaskMembership.apply(boundTo)) {
					error(result, where,
							"Referenced thread " + task.getName() + " is not bound to a declared " + fieldName);
					return null;
				}
				return boundTo;
			}
		}
	}

	// ======================================================================
	// == Error reporting methods for the visitor
	// ==
	// == XXX: Taken from NewBusLoadAnalysis --- should possibly move to the superclass
	// ======================================================================

	private static void error(final Result result, final EObject io, final String msg) {
		result.getDiagnostics().add(ResultUtil.createDiagnostic(msg, io, DiagnosticType.ERROR));
	}

	private static void warning(final Result result, final EObject io, final String msg) {
		result.getDiagnostics().add(ResultUtil.createDiagnostic(msg, io, DiagnosticType.WARNING));
	}

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

