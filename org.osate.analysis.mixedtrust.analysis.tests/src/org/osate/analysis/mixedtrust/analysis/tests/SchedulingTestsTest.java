package org.osate.analysis.mixedtrust.analysis.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instantiation.InstantiateModel;
import org.osate.analysis.mixedtrust.analysis.MixedTrustAnalysis;
import org.osate.result.AnalysisResult;
import org.osate.result.Diagnostic;
import org.osate.result.DiagnosticType;
import org.osate.result.Result;
import org.osate.result.util.ResultUtil;
import org.osate.testsupport.Aadl2InjectorProvider;
import org.osate.testsupport.TestHelper;

import com.google.inject.Inject;

@RunWith(XtextRunner.class)
@InjectWith(Aadl2InjectorProvider.class)
public class SchedulingTestsTest {
	private static final int HYPER_VISOR_PATH = 2;
	private static final int GUEST_OS_PATH = 1;
	private static final int TASK_NAME = 0;
	private static final char DOT_CHAR = '.';
	private static final char UNDERSCORE_CHAR = '_';

	private static final String INSTANCE_FORMAT = "%s_Instance";

	private static final String TOP_X = "top.x";

	private static final String SCHEDULING_TESTS_FILE = "org.osate.analysis.mixedtrust.analysis.tests/models/SchedulingTests/ScheduleMixedTrustTasks.aadl";

	@Inject
	TestHelper<AadlPackage> testHelper;

	@Test
	public void mixedTrustBindingsTest01() throws Exception {
		final SystemInstance instance = getSystemInstance(SCHEDULING_TESTS_FILE, TOP_X);
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		assertEquals(0, somResult.getDiagnostics().size());
		assertEquals(3, somResult.getSubResults().size());

		final Result procResult1 = somResult.getSubResults().get(0);
		checkProcessorResult(procResult1, followPath(instance, "s1", "P"), true, 2);
		checkTaskResult(procResult1.getSubResults().get(0), "MT 1", 8000,
				followPath(instance, "s1", "MixedTrust1", "GuestThread").getInstanceObjectPath(),
				followPath(instance, "s1", "MixedTrust1", "HyperThread").getInstanceObjectPath());
		checkTaskResult(procResult1.getSubResults().get(1), "MT 2", 11000,
				followPath(instance, "s1", "MixedTrust2", "GuestThread").getInstanceObjectPath(),
				followPath(instance, "s1", "MixedTrust2", "HyperThread").getInstanceObjectPath());

		final Result procResult2 = somResult.getSubResults().get(1);
		checkProcessorResult(procResult2, followPath(instance, "s2", "P"), true, 3);
		checkTaskResult(procResult2.getSubResults().get(0), "MT 1", 8000,
				followPath(instance, "s2", "MixedTrust1", "GuestThread").getInstanceObjectPath(),
				followPath(instance, "s2", "MixedTrust1", "HyperThread").getInstanceObjectPath());
		checkTaskResult(procResult2.getSubResults().get(1), "MT 2", 12000,
				followPath(instance, "s2", "MixedTrust2", "GuestThread").getInstanceObjectPath(),
				followPath(instance, "s2", "MixedTrust2", "HyperThread").getInstanceObjectPath());
		checkTaskResult(procResult2.getSubResults().get(2), "MT 3", 19000,
				followPath(instance, "s2", "MixedTrust3", "GuestThread").getInstanceObjectPath(),
				followPath(instance, "s2", "MixedTrust3", "HyperThread").getInstanceObjectPath());

		final Result procResult3 = somResult.getSubResults().get(2);
		checkProcessorResult(procResult3, followPath(instance, "s3", "P"), false, 0);

//		// =============
//
//		final Result procResult3 = somResult.getSubResults().get(2);
//		assertEquals(followPath(instance, "s3", "P"), procResult3.getModelElement());
//		assertEquals("top_x_Instance.s3.P", procResult3.getMessage());
//		assertEquals(false, ResultUtil.getBoolean(procResult3, 0));
//		assertEquals(0, procResult3.getDiagnostics().size());
//		assertEquals(0, procResult3.getSubResults().size());
//
//		// =============
//
//		final Result procResult2 = somResult.getSubResults().get(1);
//		assertEquals(p1, procResult2.getModelElement());
//		assertEquals("top_x_Instance.s1.P", procResult2.getMessage());
//		assertEquals(true, ResultUtil.getBoolean(procResult2, 0));
//		assertEquals(0, procResult2.getDiagnostics().size());
//		assertEquals(0, procResult2.getSubResults().size());
//
	}

	// ================================================================================
	// ================================================================================
	// ================================================================================
	// ================================================================================

	private static ComponentInstance followPath(final SystemInstance si, String... names) {
		ComponentInstance current = si;
		for (final String nextName : names) {
			final Optional<ComponentInstance> next = current.getComponentInstances()
					.stream()
					.filter(ci -> ci.getName().equals(nextName))
					.findFirst();
			if (next.isEmpty()) {
				return null;
			} else {
				current = next.get();
			}
		}
		return current;
	}

	private static void checkProcessorResult(final Result result, final ComponentInstance processor,
			final boolean isSchedulable, final int numTasks) {
		assertEquals(processor, result.getModelElement());
		assertEquals(processor.getInstanceObjectPath(), result.getMessage());
		assertEquals(isSchedulable, ResultUtil.getBoolean(result, 0));
		assertEquals(0, result.getDiagnostics().size());
		assertEquals(numTasks, result.getSubResults().size());
	}

	private static void checkTaskResult(final Result result, final String taskName, final int eValue,
			final String guestOsPath, final String hyperVisorPath) {
		// N.B. Should check the modelElement is correct, but getting the property value is too hard.
		assertEquals(taskName, result.getMessage());
		assertEquals(eValue, ResultUtil.getInteger(result, TASK_NAME));
		assertEquals(guestOsPath, ResultUtil.getString(result, GUEST_OS_PATH));
		assertEquals(hyperVisorPath, ResultUtil.getString(result, HYPER_VISOR_PATH));
		assertEquals(0, result.getDiagnostics().size());
		assertEquals(0, result.getSubResults().size());
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

	private static final void assertContainsDiagnostics(final EList<Diagnostic> actualDiagnostics,
			final ExpectedDiagnostic... expectedDiagnostics) {
		assertEquals(expectedDiagnostics.length, actualDiagnostics.size());
		for (final ExpectedDiagnostic e : expectedDiagnostics) {
			assertContainsDiagnostic(actualDiagnostics, e);
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