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
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instantiation.InstantiateModel;
import org.osate.analysis.mixedtrust.analysis.MixedTrustAnalysis;
import org.osate.result.AnalysisResult;
import org.osate.result.Diagnostic;
import org.osate.result.Result;
import org.osate.testsupport.Aadl2InjectorProvider;
import org.osate.testsupport.TestHelper;

import com.google.inject.Inject;

@RunWith(XtextRunner.class)
@InjectWith(Aadl2InjectorProvider.class)
public class ConsitencyTestsTest {
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
		assertEquals(0, diagnostics.size());
	}

	@Test
	public void test02() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 2));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertEquals(0, diagnostics.size());
	}

	@Test
	public void test03() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 2));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertEquals(0, diagnostics.size());
	}

	@Test
	public void test04() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 3));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertEquals(0, diagnostics.size());
	}

	@Test
	public void test11() throws Exception {
		final SystemInstance instance = getSystemInstance(MIXED_TRUST_BINDINGS_FILE, String.format(TOP_T_FORMAT, 11));
		final AnalysisResult analysisResult = new MixedTrustAnalysis().invoke(null, instance);
		final Result somResult = analysisResult.getResults().get(0);

		final Element badRecord = findPA(
				findNamed(instance.getComponentInstances(), "p").getOwnedPropertyAssociations(),
				"Mixed_Trust_Processor").getOwnedValues().get(0).getOwnedValue();

		final EList<Diagnostic> diagnostics = somResult.getDiagnostics();
		assertEquals(1, diagnostics.size());
		assertDiagnosticsContainsError(diagnostics, badRecord,
				"Mixed_Trust_Bindings must specifiy a value for field GuestOS");
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

	private static final void assertDiagnosticsContainsError(final EList<Diagnostic> diagnostics, final Element where,
			final String msg) {
		for (final Diagnostic d : diagnostics) {
			if (d.getModelElement().equals(where) && d.getMessage().equals(msg)) {
				return;
			}
		}
		fail(String.format("Couldn't find error \"%s\"", msg));
	}
}