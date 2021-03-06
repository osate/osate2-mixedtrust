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

package org.osate.analysis.mixedtrust.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.contrib.aadlproject.TimeUnits;
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
import org.osate.pluginsupport.properties.PropertyUtils;
import org.osate.result.AnalysisResult;
import org.osate.result.Result;
import org.osate.result.ResultType;
import org.osate.result.util.ResultUtil;
import org.osate.xtext.aadl2.properties.util.InstanceModelUtil;

import edu.cmu.sei.mtzsrm.LayeredTrustExactScheduler;

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
 *           <li>message = The component path of the processor
 *           <li>values[0] = TRUE or FALSE indicating whether the processor's tasks are schedulable (BooleanValue)
 *           <li>diagnostics = empty list
 *           <li>subResults = one {@Result} for each mixed trust task that is bound to the processor via a <code>Mixed_Trust_Properties::Mixed_Trust_Tasks</code>
 *           property association.  Results are in the order that the <code>Mixed_Trust_Task</code> records appear in the list associated with the property.
 *           The subResults list only exists if the tasks are schedulable.
 *             <ul>
 *               <li>modelElement = The Aadl EMF PropertyValue object that corresponds to the task record property value
 *               <li>resultType = SUCCESS
 *               <li>message =  name of the mixed trust task as taken from the name field of the record
 *               <li>values[0] = The E value for the mixed trust task in microseconds (IntegerValue)
 *               <li>values[1] = The component path of the mixed trust task's guest task thread (StringValue)
 *               <li>values[2] = The component path of the mixed trust task's hyper task thread (StringValue)
 *               <li>diagnostics = empty list
 *               <li>subResults = empty list
 *             </ul>
 *         </ul>
 *     </ul>
 * </ul>
 *
 * @since 4.0
 */
// TODO: Document how the consistency checking works
public final class MixedTrustAnalysis {
	private static final String ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE = "Virtual processor %s referenced by field %s is bound to more than 1 processor";
	private static final String ERR_MIXED_TRUST_BINDINGS_NOT_BOUND = "Virtual processor %s referenced by field %s is not bound to processor %s";
	private static final String ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD = "Mixed_Trust_Bindings must specifiy a value for field %s";
	private static final String ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING = "Component %s is not a GuestOS or HyperVisor but is bound to processor %s";
	private static final String ERR_MIXED_TRUST_BINDINGS_SAME_VALUE = "GuestOS and HyperVisor specify the same value %s";

	private static final String ERR_MIXED_TRUST_TASK_THREAD_NOT_BOUND_TO_RECOGNIZED = "Mixed_Trust_Task's referenced %s thread %s is not bound to a declared %s";
	private static final String ERR_MIXED_TRUST_TASK_THREAD_BOUND_TO_MORE_THAN_ONE = "Mixed_Trust_Task's referenced %s thread %s bound to more than one component";
	private static final String ERR_MIXED_TRUST_TASK_UNBOUND_THREAD = "Mixed_Trust_Task's referenced %s thread %s is not bound";
	private static final String ERR_MIXED_TRUST_TASK_BOUND_TO_DIFFERENT_PROCESSORS = "Mixed_Trust_Task's GuestTask and HyperTask are bound to different processors";
	private static final String ERR_MIXED_TRUST_TASK_MUST_SPECIFY_FIELD = "Mixed_Trust_Task must specify a value for field %s";
	private static final String ERR_MIXED_TRUST_TASK_NO_EXECUTION_TIME = "Mixed_Trust_Task's referenced %s thread %s does not have an association for Compute_Execution_Time";

	private static final String ERR_BOUND_BUT_NOT_IDENTIFIED = "%s is bound to %s %s but is not identified in a Mixed_Trust_Task record";

	private static final String WARNING_MIXED_TRUST_TASK_SPECIFIES_VALUE = "Mixed_Trust_Task's referenced %s thread %s specifies a %s value";

	private static final String DEADLINE = "Deadline";
	private static final String PERIOD = "Period";
	private static final String HYPER_VISOR = "HyperVisor";
	private static final String GUEST_OS = "GuestOS";
	private static final String GUEST_TASK = "GuestTask";
	private static final String HYPER_TASK = "HyperTask";

	private static final String EMPTY_STRING = "";
	private static final String ANALYSIS_RESULT_MESSAGE = "Mixed Trust Scheduling of %s";
	private static final String ANALYSIS_RESULT_LABEL = "Mixed Trust Scheduling";

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

	private AnalysisResult analyzeBody(final IProgressMonitor monitor, final SystemInstance systemInstance) {
		final AnalysisResult analysisResult = ResultUtil.createAnalysisResult(ANALYSIS_RESULT_LABEL, systemInstance);
		analysisResult.setResultType(ResultType.SUCCESS);
		analysisResult.setMessage(String.format(ANALYSIS_RESULT_MESSAGE, systemInstance.getFullName()));

		final SOMIterator soms = new SOMIterator(systemInstance);
		while (soms.hasNext()) {
			// TODO: Check monitor for being cancelled

			/*
			 * XXX: General question, do system operation modes mess up property lookup? Are we guaranteed that the referenced
			 * components actually exist in the som?
			 */

			final SystemOperationMode som = soms.nextSOM();
			final Result somResult = ResultUtil.createResult(
					Aadl2Util.isPrintableSOMName(som) ? Aadl2Util.getPrintableSOMMembers(som) : EMPTY_STRING, som,
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
						if (checkMixedTrustBindings(somResult, where, processor, mixedTrustBindings, domains)) {
							domains.addMixedTrustProcessor(processor);
						}
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

			/* Check that nothing extra is bound to the GuestOS and hypervisors */
			domains.checkForExtraBindings(systemInstance, somResult);

			/*
			 * Now we can actually run the scheduling. We create a scheduler object for each processor
			 * identified with a consistent Mixed_Trust_Bindings property association.
			 */
			for (final InstanceObject processor : domains.getMixedTrustProcessors()) {
				final Result processorResult = ResultUtil.createResult(processor.getInstanceObjectPath(), processor,
						ResultType.SUCCESS);
				somResult.getSubResults().add(processorResult);

				final LayeredTrustExactScheduler scheduler = new LayeredTrustExactScheduler();
				final List<edu.cmu.sei.mtzsrm.MixedTrustTask> taskList = new LinkedList<>();
				for (final MixedTrustTask mixedTrustTask : domains.getTasksForProcessor(processor)) {
					final var schedulerTask = createMixedTrustTask(mixedTrustTask);
					scheduler.add(schedulerTask);
					taskList.add(schedulerTask);
				}
				final boolean isSchedulable = scheduler.isSchedulable();
				ResultUtil.addBooleanValue(processorResult, isSchedulable);

				if (isSchedulable) {
					final Iterator<MixedTrustTask> mttDefIter = domains.getTasksForProcessor(processor).iterator();
					for (final edu.cmu.sei.mtzsrm.MixedTrustTask mtt : taskList) {
						final MixedTrustTask mttTaskDef = mttDefIter.next();
						final int eValue = mtt.getDeadline() - mtt.getHyperTask().getResponseTime();

						final Result mttResult = ResultUtil.createResult(mttTaskDef.getName().orElse(EMPTY_STRING),
								domains.getMixedTrustTaskSource(mttTaskDef), ResultType.SUCCESS);
						processorResult.getSubResults().add(mttResult);
						ResultUtil.addIntegerValue(mttResult, eValue);
						// Record fields have been checked for existence already, shouldn't fail
						ResultUtil.addStringValue(mttResult, mttTaskDef.getGuesttask().get().getInstanceObjectPath());
						// Record fields have been checked for existence already, shouldn't fail
						ResultUtil.addStringValue(mttResult, mttTaskDef.getHypertask().get().getInstanceObjectPath());
					}
				}
			}



		}
		monitor.done();

		return analysisResult;
	}

	private edu.cmu.sei.mtzsrm.MixedTrustTask createMixedTrustTask(final MixedTrustTask mixedTrustTask) {
		// get the period, deadline, and execution times in microseconds
		final int period = mixedTrustTask.getPeriod().map(v -> v.getValue(TimeUnits.US)).orElse(0.0).intValue();
		final int deadline = mixedTrustTask.getDeadline().map(v -> v.getValue(TimeUnits.US)).orElse(0.0).intValue();
		final int guestExecTime = mixedTrustTask.getGuesttask()
				.map(thread -> PropertyUtils
						.getScaledRange(TimingProperties::getComputeExecutionTime, thread, TimeUnits.US)
						.map(range -> range.getMaximum())
						.orElse(0.0))
				.orElse(0.0)
				.intValue();
		final int hyperExecTime = mixedTrustTask.getHypertask()
				.map(thread -> PropertyUtils
						.getScaledRange(TimingProperties::getComputeExecutionTime, thread, TimeUnits.US)
						.map(range -> range.getMaximum())
						.orElse(0.0))
				.orElse(0.0)
				.intValue();

		// NB. Guest task criticality must always be 0; hypertask criticality must always be 1
		return new edu.cmu.sei.mtzsrm.MixedTrustTask(period, deadline, 0, new int[] { guestExecTime }, 1, hyperExecTime,
				0);
	}

	// ======================================================================
	// === Consistency Checking Methods
	// ======================================================================

	/* Returns true if the property association is good enough for the processor to be added to the analysis model */
	private boolean checkMixedTrustBindings(final Result result, final EObject where, final ComponentInstance processor,
			final MixedTrustBindings mixedTrustBindings, final Domains domains) {
		boolean isBindingOkay = true;
		final ComponentInstance guestOS = (ComponentInstance) mixedTrustBindings.getGuestos().orElse(null);
		final ComponentInstance hyperVisor = (ComponentInstance) mixedTrustBindings.getHypervisor().orElse(null);

		isBindingOkay &= checkVirtualProcessor(result, where, processor, guestOS, GUEST_OS);
		isBindingOkay &= checkVirtualProcessor(result, where, processor, hyperVisor, HYPER_VISOR);

		if (guestOS != null && guestOS == hyperVisor) {
			Object[] args = { guestOS.getName() };
			ResultUtil.addError(result, where, ERR_MIXED_TRUST_BINDINGS_SAME_VALUE, args);
			isBindingOkay = false;
		}

		if (guestOS != null && hyperVisor != null) {
			/* Check that only guestOS and hyperVisor are bound to the processor */
			for (final ComponentInstance ci : getBoundVirtualProcessors(processor)) {
				if (ci != guestOS && ci != hyperVisor) {
					Object[] args = { ci.getName(), processor.getName() };
					ResultUtil.addError(result, processor, ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING, args);
					isBindingOkay = false;
				}
			}

			domains.addGuestOS(guestOS);
			domains.addHyperVisor(hyperVisor);
		}
		return isBindingOkay;
	}

	/* Returns true if the virtual processor reference is acceptable */
	private boolean checkVirtualProcessor(final Result result, final EObject where, final ComponentInstance processor,
			final InstanceObject virtualProc, final String fieldName) {
		boolean isReferenceOkay = true;
		if (virtualProc == null) {
			Object[] args = { fieldName };
			ResultUtil.addError(result, where, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, args);
			isReferenceOkay = false;
		} else {
			final List<InstanceObject> processorBindings = getProcessorBindings(virtualProc);
			if (!processorBindings.contains(processor)) {
				// error: not directly bound to processor
				Object[] args = { virtualProc.getName(), fieldName, processor.getName() };
				ResultUtil.addError(result, where, ERR_MIXED_TRUST_BINDINGS_NOT_BOUND, args);
				isReferenceOkay = false;
			}
			if (countBoundProcessors(processorBindings) > 1) {
				Object[] args = { virtualProc.getName(), fieldName };
				ResultUtil.addError(result, where, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, args);
				isReferenceOkay = false;
			}
		}
		return isReferenceOkay;
	}

	private int countBoundProcessors(final List<InstanceObject> bindings) {
		int count = 0;
		for (final InstanceObject io : bindings) {
			if (io instanceof ComponentInstance) {
				final var cc = ((ComponentInstance) io).getCategory();
				if (cc == ComponentCategory.VIRTUAL_PROCESSOR || cc == ComponentCategory.PROCESSOR) {
					count += 1;
				}
			}
		}
		return count;
	}

	/**
	 * Check the consistency of a Multi_Trust_Task record.
	 * @param where The EObject of the actual property value in the AADL EMF model
	 * @param MixedTrustTask the corresponding record object that is derived from that property value
	 * @return {@code true} iff the task record is good enough to be added to the analysis model.
	 */
	private boolean checkMixedTrustTask(final Result result, final EObject where, final MixedTrustTask mtt,
			final Domains domains) {
		boolean isTaskOkay = true;
		if (mtt.getPeriod().isEmpty()) {
			Object[] args = { PERIOD };
			ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_MUST_SPECIFY_FIELD, args);
			isTaskOkay = false;
		}
		if (mtt.getDeadline().isEmpty()) {
			Object[] args = { DEADLINE };
			ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_MUST_SPECIFY_FIELD, args);
			isTaskOkay = false;
		}

		final ComponentInstance guestTask = (ComponentInstance) mtt.getGuesttask().orElse(null);
		final ComponentInstance hyperTask = (ComponentInstance) mtt.getHypertask().orElse(null);
		final InstanceObject guestOsBinding = checkTask(result, where, guestTask, domains::isGuestOS,
				domains::addBoundGuestTask, GUEST_TASK);
		final InstanceObject hyperVisorBinding = checkTask(result, where, hyperTask, domains::isHyperVisor,
				domains::addBoundHypertTask, HYPER_TASK);
		if (guestOsBinding != null && hyperVisorBinding != null) {
			/*
			 * If the multi trust task has guest os task and a hyper task, and those tasks are bound to a known
			 * guest os and hyper visor, then check that the guest os and hyper visor are both bound to the
			 * same processor. No fair splitting the multi trust task across two different processors.
			 */
			final List<InstanceObject> boundProcs1 = getProcessorBindings(guestOsBinding);
			final List<InstanceObject> boundProcs2 = getProcessorBindings(hyperVisorBinding);

			if (boundProcs1.size() == 1 && boundProcs2.size() == 1) {
				if (boundProcs1.get(0) != boundProcs2.get(0)) {
					Object[] args = {};
					ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_BOUND_TO_DIFFERENT_PROCESSORS, args);
					isTaskOkay = false;
				} else {
					/*
					 * Both are bound to virtual processors that are bound to the same processor, so we add the mixed
					 * trust task to the set of tasks for that processor.
					 */
					domains.addMixedTrustTask((ComponentInstance) boundProcs1.get(0), mtt, where);
				}
			}
		} else {
			isTaskOkay = false;
		}

		return isTaskOkay;
	}

	/**
	 * Check the consistency of the Thread ComponentInstance identified by {@code task}.
	 * @return {@code true} The virtual processor the thread is bound iff the thread is consistent; {@code null} otherwise.
	 */
	private InstanceObject checkTask(final Result result, final EObject where, final ComponentInstance task,
			final Function<ComponentInstance, Boolean> checkTaskMembership,
			final BiFunction<ComponentInstance, ComponentInstance, Boolean> addBoundTask, final String fieldName) {
		if (task == null) {
			Object[] args = { fieldName };
			ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_MUST_SPECIFY_FIELD, args);
			return null;
		} else {
			boolean isOkay = true;

			if (TimingProperties.getComputeExecutionTime(task).isEmpty()) {
				Object[] args = { fieldName, task.getName() };
				ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_NO_EXECUTION_TIME, args);
				isOkay = false;
			}
			if (TimingProperties.getPeriod(task).isPresent()) {
				Object[] args = { fieldName, task.getName(), PERIOD };
				ResultUtil.addWarning(result, where, WARNING_MIXED_TRUST_TASK_SPECIFIES_VALUE, args);
			}
			if (TimingProperties.getDeadline(task).isPresent()) {
				Object[] args = { fieldName, task.getName(), DEADLINE };
				ResultUtil.addWarning(result, where, WARNING_MIXED_TRUST_TASK_SPECIFIES_VALUE, args);
			}

			final List<InstanceObject> boundProcs = getProcessorBindings(task);
			if (boundProcs.isEmpty()) {
				Object[] args = { fieldName, task.getName() };
				ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_UNBOUND_THREAD, args);
				return null;
			} else if (boundProcs.size() > 1) {
				Object[] args = { fieldName, task.getName() };
				ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_THREAD_BOUND_TO_MORE_THAN_ONE, args);

				for (final InstanceObject p : boundProcs) {
					addBoundTask.apply((ComponentInstance) p, task);
				}
				return null;
			} else {
				final ComponentInstance boundTo = (ComponentInstance) boundProcs.get(0);
				if (!checkTaskMembership.apply(boundTo)) {
					Object[] args = { fieldName, task.getName(), fieldName };
					ResultUtil.addError(result, where, ERR_MIXED_TRUST_TASK_THREAD_NOT_BOUND_TO_RECOGNIZED, args);
					isOkay = false;
				} else {
					addBoundTask.apply(boundTo, task);
				}
				return isOkay ? boundTo : null;
			}
		}
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
			if (parent.getCategory() == ComponentCategory.PROCESSOR
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

	/*
	 * This class became more complicated than I intended. Probably means I really should have had a
	 * formal analysis model for this analysis.
	 */
	private static final class Domains {
		/* Use lists to guarantee a consistent order in the output of results. */

		/**
		 * Map from Processor ComponentInstances to Lists of the mixed trust tasks associated with the processor.
		 * Only processors that pass consistency checking are added as keys, and only tasks that pass consistency
		 * checking are added to the list.
		 */
		private final Map<ComponentInstance, List<MixedTrustTask>> mixedTrustProcessors = new HashMap<>();

		/**
		 * Map from Virtual Processor ComponentInstances representing guest operating systems to List of
		 * ComponentInstances of Threads bound to that guest os.  Only guest os virtual processors that pass
		 * consistency checking are added as keys, and only guest task threads that pass consistency checking are
		 * added to the list.
		 */
		private final Map<ComponentInstance, List<ComponentInstance>> guestOSes = new HashMap<>();

		/**
		 * Map from Virtual Processor ComponentInstances representing hyper visors to List of
		 * ComponentInstances of Threads bound to that hyper visor.  Only hyper visor virtual processors that pass
		 * consistency checking are added as keys, and only hyper task threads that pass consistency checking are
		 * added to the list.
		 */
		private final Map<ComponentInstance, List<ComponentInstance>> hyperVisors = new HashMap<>();

		/**
		 * Map from MixedTrustTask records to the actual AADL EMF property value object that the record
		 * is derived from.  This is used for error and result reporting.
		 */
		private final Map<MixedTrustTask, EObject> taskRecords = new HashMap<>();

		/**
		 * List of all the processors with consistent mixed trust bindings.  THese are the
		 * same objects that are in the keyset of {@link #mixedTrustProcessors}.  This list is
		 * maintained separately so that we can visit the processors in the order in which they
		 * appear in the source text and produce an output with a consistent order.
		 */
		private final List<ComponentInstance> processorList = new ArrayList<>();

		public void addGuestOS(final ComponentInstance guestOS) {
			guestOSes.put(guestOS, new ArrayList<>());
		}

		public void addHyperVisor(final ComponentInstance hyperVisor) {
			hyperVisors.put(hyperVisor, new ArrayList<>());
		}

		public boolean isGuestOS(final InstanceObject task) {
			return guestOSes.containsKey(task);
		}

		public boolean isHyperVisor(final InstanceObject task) {
			return hyperVisors.containsKey(task);
		}

		public void addMixedTrustProcessor(final ComponentInstance processor) {
			processorList.add(processor);
			mixedTrustProcessors.put(processor, new ArrayList<>());
		}

		private static <A, B> boolean addToMappedList(final Map<A, List<B>> map, final A key, final B value) {
			final List<B> set = map.get(key);
			if (set != null) {
				set.add(value);
				return true;
			} else {
				return false;
			}
		}

		public boolean addBoundGuestTask(final ComponentInstance guestOS, final ComponentInstance guestTask) {
			return addToMappedList(guestOSes, guestOS, guestTask);
		}

		public boolean addBoundHypertTask(final ComponentInstance hyperVisor, final ComponentInstance hyperTask) {
			return addToMappedList(hyperVisors, hyperVisor, hyperTask);
		}

		public boolean addMixedTrustTask(final ComponentInstance processor, final MixedTrustTask mixedTrustTask,
				final EObject where) {
			taskRecords.put(mixedTrustTask, where);
			return addToMappedList(mixedTrustProcessors, processor, mixedTrustTask);
		}

		public List<ComponentInstance> getMixedTrustProcessors() {
			return Collections.unmodifiableList(processorList);
		}

		public Iterable<MixedTrustTask> getTasksForProcessor(final InstanceObject processor) {
			return mixedTrustProcessors.get(processor);
		}

		public EObject getMixedTrustTaskSource(final MixedTrustTask mtt) {
			return taskRecords.get(mtt);
		}

		/**
		 * For each thread and virtual processor X in the given system, check that if that if X is bound
		 * to a known Guest operating system or hypervisor, then X much be identified as such via a
		 * Mixed_Trust_Task record.  In other words, only the virtual processors identified in
		 * Mixed_Trust_Task records are bound to processors with Mixed_Trust_Task records.  Nothing
		 * extra is allowed.
		 *
		 * @param systemInstance
		 * @param result
		 * @return {@code true} iff there are no extra bindings
		 */
		public boolean checkForExtraBindings(final SystemInstance systemInstance, final Result result) {
			boolean isOkay = true;
			for (final ComponentInstance ci : systemInstance.getAllComponentInstances()) {
				final ComponentCategory cc = ci.getCategory();
				// Look up what each thread and virtual processor is bound to
				if (cc == ComponentCategory.THREAD || cc == ComponentCategory.VIRTUAL_PROCESSOR) {
					final List<InstanceObject> bindings = getProcessorBindings(ci);
					// if it is bound to a known GuestOS or HyperVisor, then it must be identified as such in a Mixed_Trust_Task
					isOkay &= checkIfDeclaredBinding(GUEST_OS, guestOSes, ci, bindings, result);
					isOkay &= checkIfDeclaredBinding(HYPER_VISOR, hyperVisors, ci, bindings, result);
				}
			}
			return isOkay;
		}

		private static boolean checkIfDeclaredBinding(final String domainName,
				final Map<ComponentInstance, List<ComponentInstance>> domainMap,
				final InstanceObject threadOrVP, final List<InstanceObject> bindings, final Result result) {
			boolean isOkay = true;
			for (final InstanceObject boundTo : bindings) {
				final List<ComponentInstance> declaredBindings = domainMap.get(boundTo);
				if (declaredBindings != null && !declaredBindings.contains(threadOrVP)) {
					Object[] args = { threadOrVP.getName(), domainName, boundTo.getName() };
					ResultUtil.addError(result, boundTo, ERR_BOUND_BUT_NOT_IDENTIFIED, args);
					isOkay = false;
				}
			}
			return isOkay;
		}
	}
}

