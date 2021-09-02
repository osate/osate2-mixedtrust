package org.osate.analysis.mixedtrust.analysis.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.Element;
import org.osate.aadl2.PropertyAssociation;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instantiation.InstantiateModel;
import org.osate.analysis.mixedtrust.analysis.MixedTrustAnalysis;
import org.osate.result.AnalysisResult;
import org.osate.result.Diagnostic;
import org.osate.result.DiagnosticType;
import org.osate.result.Result;
import org.osate.testsupport.Aadl2InjectorProvider;
import org.osate.testsupport.TestHelper;

import com.google.inject.Inject;

@RunWith(XtextRunner.class)
@InjectWith(Aadl2InjectorProvider.class)
public class ConsitencyTestsTest {
	private static final String OTHER = "Other";
	private static final String OTHER2 = "Other2";
	private static final String COMP_P = "p";
	private static final String MIXED_TRUST_PROCESSOR = "Mixed_Trust_Processor";
	private static final String ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE = "Virtual processor %s referenced by field %s is bound to more than 1 processor";
	private static final String ERR_MIXED_TRUST_BINDINGS_NOT_BOUND = "Virtual processor %s referenced by field %s is not bound to processor %s";
	private static final String ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD = "Mixed_Trust_Bindings must specifiy a value for field %s";
	private static final String ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING = "Component %s is not a GuestOS or HyperVisor but is bound to processor %s";
	private static final String ERR_MIXED_TRUST_BINDINGS_SAME_VALUE = "GuestOS and HyperVisor specify the same value %s";

	private static final String ERR_MIXED_TRUST_TASK_THREAD_NOT_BOUND_TO_RECOGNIZED = "Mixed_Trust_Task's referenced %s thread %s is not bound to a declared %s";
	private static final String ERR_MIXED_TRUST_TASK_THREAD_BOUND_TO_MORE_THAN_ONE = "Mixed_Trust_Task's referenced %s thread %s bound to more than one component";
	private static final String ERR_MIXED_TRUST_TASK_UNBOUND_THREAD = "Mixed_Trust_Task's referenced %s thread %s is not bound";
	private static final String ERR_MIXED_TRUST_TASK_BOUND_TO_DIFFERENT_PROCESSORS = "Mixed_Trust_Task's GuestOS and HyperVisor are bound to different processors";
	private static final String ERR_MIXED_TRUST_TASK_MUST_SPECIFY_FIELD = "Mixed_Trust_Task must specify a value for field %s";

	private static final String WARNING_MIXED_TRUST_TASK_SPECIFIES_VALUE = "Mixed_Trust_Task's referenced %s thread %s specifies a %s value";

	private static final String DEADLINE = "Deadline";
	private static final String PERIOD = "Period";
	private static final String HYPER_VISOR = "HyperVisor";
	private static final String GUEST_OS = "GuestOS";

	private static final String MIXED_TRUST_BINDINGS_FILE = "org.osate.analysis.mixedtrust.analysis.tests/models/ConsistencyTests/TestMixedTrustBindings.aadl";

	private static final char DOT_CHAR = '.';
	private static final char UNDERSCORE_CHAR = '_';

	private static final String INSTANCE_FORMAT = "%s_Instance";

	private static final String TOP_T_FORMAT = "top.t%02d";

	@Inject
	TestHelper<AadlPackage> testHelper;

	@Test
	public void test01() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 1));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Collections.emptyList(), diagnostics);
	}

	@Test
	public void test02() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 2));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Collections.emptyList(), diagnostics);
	}

	@Test
	public void test03() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 2));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Collections.emptyList(), diagnostics);
	}

	@Test
	public void test04() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 3));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Collections.emptyList(), diagnostics);
	}

	// ================================================================================

	@Test
	public void test11() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 11));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, GUEST_OS)),
				diagnostics);
	}

	@Test
	public void test12() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 12));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, GUEST_OS)),
				diagnostics);
	}

	@Test
	public void test13() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 13));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, GUEST_OS)),
				diagnostics);
	}

	@Test
	public void test14() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 14));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, GUEST_OS)),
				diagnostics);
	}

	// ================================================================================

	@Test
	public void test21() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 21));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, HYPER_VISOR)),
				diagnostics);
	}

	@Test
	public void test22() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 22));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, HYPER_VISOR)),
				diagnostics);
	}

	@Test
	public void test23() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 23));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, HYPER_VISOR)),
				diagnostics);
	}

	@Test
	public void test24() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 24));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_MUST_SPECIFY_FIELD, HYPER_VISOR)),
				diagnostics);
	}

	// ================================================================================

	@Test
	public void test31() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 31));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_NOT_BOUND, OTHER, GUEST_OS, COMP_P)),
				diagnostics);
	}

	@Test
	public void test33() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 33));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_NOT_BOUND, OTHER, GUEST_OS, COMP_P)),
				diagnostics);
	}

	// ================================================================================

	@Test
	public void test41() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 41));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Collections.singletonList(
				error(badRecord, ERR_MIXED_TRUST_BINDINGS_NOT_BOUND, OTHER, HYPER_VISOR, COMP_P)), diagnostics);
	}

	@Test
	public void test42() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 42));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Collections.singletonList(
				error(badRecord, ERR_MIXED_TRUST_BINDINGS_NOT_BOUND, OTHER, HYPER_VISOR, COMP_P)), diagnostics);
	}

	// ================================================================================

	@Test
	public void test51() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 51));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_SAME_VALUE, OTHER)), diagnostics);
	}

	// ================================================================================

	@Test
	public void test61() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 61));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findNamed(instance.getComponentInstances(), COMP_P);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING, OTHER, COMP_P)),
				diagnostics);
	}

	@Test
	public void test62() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 62));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findNamed(instance.getComponentInstances(), COMP_P);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING, OTHER, COMP_P)),
				diagnostics);
	}

	@Test
	public void test63() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 63));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findNamed(instance.getComponentInstances(), COMP_P);
		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(Arrays.asList(error(badRecord, ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING, OTHER, COMP_P),
				error(badRecord, ERR_MIXED_TRUST_BINDINGS_EXTRA_BINDING, OTHER2, COMP_P)), diagnostics);
	}

	// ================================================================================

	@Test
	public void test71() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 71));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(
						error(badRecord, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, GUEST_OS, GUEST_OS)),
				diagnostics);
	}

	@Test
	public void test72() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 72));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(
						error(badRecord, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, GUEST_OS, GUEST_OS)),
				diagnostics);
	}

	@Test
	public void test73() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 73));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(
						error(badRecord, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, GUEST_OS, GUEST_OS)),
				diagnostics);
	}

	// ================================================================================

	@Test
	public void test81() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 81));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(
						error(badRecord, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, HYPER_VISOR, HYPER_VISOR)),
				diagnostics);
	}

	@Test
	public void test82() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 82));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(
						error(badRecord, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, HYPER_VISOR, HYPER_VISOR)),
				diagnostics);
	}

	@Test
	public void test83() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 83));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), COMP_P).getOwnedPropertyAssociations(),
				MIXED_TRUST_PROCESSOR).getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertContainsDiagnostics(
				Collections.singletonList(
						error(badRecord, ERR_MIXED_TRUST_BINDINGS_BOUND_TO_MORE_THAN_ONE, HYPER_VISOR, HYPER_VISOR)),
				diagnostics);
	}

	// ================================================================================
	// ================================================================================
	// ================================================================================
	// ================================================================================

	private static class ExpectedDiagnostic {
		final DiagnosticType expectedType;
		final Element expectedLocation;
		final String expectedMsg;

		public ExpectedDiagnostic(final DiagnosticType type, final Element loc, final String msg) {
			expectedType = type;
			expectedLocation = loc;
			expectedMsg = msg;
		}

		public boolean equals(final Diagnostic d) {
			return expectedLocation.equals(d.getModelElement()) && expectedType.equals(d.getDiagnosticType())
					&& expectedMsg.equals(d.getMessage());
		}
	}

	private ExpectedDiagnostic error(final Element where, final String formatMsg, final Object... args) {
		return new ExpectedDiagnostic(DiagnosticType.ERROR, where, String.format(formatMsg, args));
	}

	private ExpectedDiagnostic warning(final Element where, final String formatMsg, final Object... args) {
		return new ExpectedDiagnostic(DiagnosticType.WARNING, where, String.format(formatMsg, args));
	}

	private SystemInstance getSystemInstance(final String aadlFile, final String systemImplName) throws Exception {
		final AadlPackage pkg = testHelper.parseFile(aadlFile);
		final Optional<Classifier> impl = pkg.getOwnedPublicSection()
				.getOwnedClassifiers()
				.stream()
				.filter(c -> c.getName().equals(systemImplName))
				.findFirst();

		final String expectedName = String.format(INSTANCE_FORMAT, systemImplName.replace(DOT_CHAR, UNDERSCORE_CHAR));
		final SystemInstance instance = InstantiateModel.instantiate((ComponentImplementation) impl.get());
		Assert.assertEquals(expectedName, instance.getName());
		return instance;
	}

	private static <T> T findNamed(final EList<T> list, final Function<T, String> getName, final String name) {
		for (final T x : list) {
			if (getName.apply(x).equals(name)) {
				return x;
			}
		}
		return null;
	}

	private static <T extends InstanceObject> T findNamed(final EList<T> list, final String name) {
		return findNamed(list, x -> x.getName(), name);
	}

	private static PropertyAssociation findPA(final EList<PropertyAssociation> list, final String property) {
		return findNamed(list, pa -> pa.getProperty().getName(), property);
	}

	private static final void assertContainsDiagnostics(final List<ExpectedDiagnostic> expected,
			final EList<Diagnostic> diagnostics) {
		assertEquals(expected.size(), diagnostics.size());
		for (final ExpectedDiagnostic e : expected) {
			assertContainsDiagnostic(diagnostics, e);
		}
	}

	private static final void assertContainsDiagnostic(final EList<Diagnostic> diagnostics,
			final ExpectedDiagnostic expected) {
		for (final Diagnostic d : diagnostics) {
			if (expected.equals(d)) {
				return;
			}
		}
		fail(String.format("Couldn't find error \"%s\"", expected.expectedMsg));
	}
}