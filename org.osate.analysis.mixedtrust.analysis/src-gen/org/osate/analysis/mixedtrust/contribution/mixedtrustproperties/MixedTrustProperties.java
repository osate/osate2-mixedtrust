/*******************************************************************************
 * Copyright (c) 2004-2021 Carnegie Mellon University and others. (see Contributors file).
 * All Rights Reserved.
 *
 * NO WARRANTY. ALL MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
 * MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Created, in part, with funding and support from the United States Government. (see Acknowledgments file).
 *
 * This program includes and/or can make use of certain third party source code, object code, documentation and other
 * files ("Third Party Software"). The Third Party Software that is used by this program is dependent upon your system
 * configuration. By using this program, You agree to comply with any and all relevant Third Party Software terms and
 * conditions contained in any such Third Party Software or separate license file distributed with such Third Party
 * Software. The parties who own the Third Party Software ("Third Party Licensors") are intended third party beneficiaries
 * to this license with respect to the terms applicable to their Third Party Software. Third Party Software licenses
 * only apply to the Third Party Software and not any other portion of this program or this program as a whole.
 *******************************************************************************/
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
