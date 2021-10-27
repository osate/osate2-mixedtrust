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

package org.osate.analysis.mixedtrust.contribution.mixedtrustproperties;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osate.aadl2.Aadl2Package;
import org.osate.aadl2.ListValue;
import org.osate.aadl2.Mode;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Property;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.modelsupport.scoping.Aadl2GlobalScopeUtil;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.pluginsupport.properties.CodeGenUtil;

public class MixedTrustProperties {
	public static final String MIXED_TRUST_PROPERTIES__NAME = "Mixed_Trust_Properties";

	public static final String MIXED_TRUST_TASKS__NAME = "Mixed_Trust_Tasks";
	public static final String MIXED_TRUST_PROCESSOR__NAME = "Mixed_Trust_Processor";

	public static Optional<List<MixedTrustTask>> getMixedTrustTasks(NamedElement lookupContext) {
		return getMixedTrustTasks(lookupContext, Optional.empty());
	}

	public static Optional<List<MixedTrustTask>> getMixedTrustTasks(NamedElement lookupContext, Mode mode) {
		return getMixedTrustTasks(lookupContext, Optional.of(mode));
	}

	public static Optional<List<MixedTrustTask>> getMixedTrustTasks(NamedElement lookupContext, Optional<Mode> mode) {
		String name = "Mixed_Trust_Properties::Mixed_Trust_Tasks";
		Property property = Aadl2GlobalScopeUtil.get(lookupContext, Aadl2Package.eINSTANCE.getProperty(), name);
		try {
			PropertyExpression value = CodeGenUtil.lookupProperty(property, lookupContext, mode);
			PropertyExpression resolved = CodeGenUtil.resolveNamedValue(value, lookupContext, mode);
			return Optional.of(((ListValue) resolved).getOwnedListElements().stream().map(element1 -> {
				PropertyExpression resolved1 = CodeGenUtil.resolveNamedValue(element1, lookupContext, mode);
				return new MixedTrustTask(resolved1, lookupContext, mode);
			}).collect(Collectors.toList()));
		} catch (PropertyNotPresentException e) {
			return Optional.empty();
		}
	}

	public static PropertyExpression getMixedTrustTasks_EObject(NamedElement lookupContext) {
		String name = "Mixed_Trust_Properties::Mixed_Trust_Tasks";
		Property property = Aadl2GlobalScopeUtil.get(lookupContext, Aadl2Package.eINSTANCE.getProperty(), name);
		return lookupContext.getNonModalPropertyValue(property);
	}

	public static Optional<MixedTrustBindings> getMixedTrustProcessor(NamedElement lookupContext) {
		return getMixedTrustProcessor(lookupContext, Optional.empty());
	}

	public static Optional<MixedTrustBindings> getMixedTrustProcessor(NamedElement lookupContext, Mode mode) {
		return getMixedTrustProcessor(lookupContext, Optional.of(mode));
	}

	public static Optional<MixedTrustBindings> getMixedTrustProcessor(NamedElement lookupContext, Optional<Mode> mode) {
		String name = "Mixed_Trust_Properties::Mixed_Trust_Processor";
		Property property = Aadl2GlobalScopeUtil.get(lookupContext, Aadl2Package.eINSTANCE.getProperty(), name);
		try {
			PropertyExpression value = CodeGenUtil.lookupProperty(property, lookupContext, mode);
			PropertyExpression resolved = CodeGenUtil.resolveNamedValue(value, lookupContext, mode);
			return Optional.of(new MixedTrustBindings(resolved, lookupContext, mode));
		} catch (PropertyNotPresentException e) {
			return Optional.empty();
		}
	}

	public static PropertyExpression getMixedTrustProcessor_EObject(NamedElement lookupContext) {
		String name = "Mixed_Trust_Properties::Mixed_Trust_Processor";
		Property property = Aadl2GlobalScopeUtil.get(lookupContext, Aadl2Package.eINSTANCE.getProperty(), name);
		return lookupContext.getNonModalPropertyValue(property);
	}
}
