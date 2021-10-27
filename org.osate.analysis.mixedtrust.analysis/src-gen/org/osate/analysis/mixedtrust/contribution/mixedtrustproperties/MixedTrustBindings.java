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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osate.aadl2.Aadl2Factory;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.Mode;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.InstanceReferenceValue;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.pluginsupport.properties.CodeGenUtil;
import org.osate.pluginsupport.properties.GeneratedRecord;

public class MixedTrustBindings extends GeneratedRecord {
	public static final String GUESTOS__NAME = "GuestOS";
	public static final String HYPERVISOR__NAME = "HyperVisor";
	public static final URI GUESTOS__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.1/@ownedField.0");
	public static final URI HYPERVISOR__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.1/@ownedField.1");

	private final Optional<InstanceObject> guestos;
	private final Optional<InstanceObject> hypervisor;

	public MixedTrustBindings(
			Optional<InstanceObject> guestos,
			Optional<InstanceObject> hypervisor
	) {
		this.guestos = guestos;
		this.hypervisor = hypervisor;
	}

	public MixedTrustBindings(PropertyExpression propertyExpression, NamedElement lookupContext, Optional<Mode> mode) {
		RecordValue recordValue = (RecordValue) propertyExpression;

		Optional<InstanceObject> guestos_local;
		try {
			guestos_local = findFieldValue(recordValue, GUESTOS__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return ((InstanceReferenceValue) resolved).getReferencedInstanceObject();
			});
		} catch (PropertyNotPresentException e) {
			guestos_local = Optional.empty();
		}
		this.guestos = guestos_local;

		Optional<InstanceObject> hypervisor_local;
		try {
			hypervisor_local = findFieldValue(recordValue, HYPERVISOR__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return ((InstanceReferenceValue) resolved).getReferencedInstanceObject();
			});
		} catch (PropertyNotPresentException e) {
			hypervisor_local = Optional.empty();
		}
		this.hypervisor = hypervisor_local;
	}

	public Optional<InstanceObject> getGuestos() {
		return guestos;
	}

	public Optional<InstanceObject> getHypervisor() {
		return hypervisor;
	}

	@Override
	public RecordValue toPropertyExpression(ResourceSet resourceSet) {
		if (!guestos.isPresent()
				&& !hypervisor.isPresent()
		) {
			throw new IllegalStateException("Record must have at least one field set.");
		}
		RecordValue recordValue = Aadl2Factory.eINSTANCE.createRecordValue();
		guestos.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, GUESTOS__URI, GUESTOS__NAME));
			fieldAssociation.setOwnedValue(CodeGenUtil.toPropertyExpression(field));
		});
		hypervisor.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, HYPERVISOR__URI, HYPERVISOR__NAME));
			fieldAssociation.setOwnedValue(CodeGenUtil.toPropertyExpression(field));
		});
		return recordValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				guestos,
				hypervisor
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MixedTrustBindings)) {
			return false;
		}
		MixedTrustBindings other = (MixedTrustBindings) obj;
		return Objects.equals(this.guestos, other.guestos)
				&& Objects.equals(this.hypervisor, other.hypervisor);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		this.guestos.ifPresent(field -> {
			builder.append(GUESTOS__NAME);
			builder.append(" => reference (");
			builder.append(field.getName());
			builder.append(");");
		});
		this.hypervisor.ifPresent(field -> {
			builder.append(HYPERVISOR__NAME);
			builder.append(" => reference (");
			builder.append(field.getName());
			builder.append(");");
		});
		builder.append(']');
		return builder.toString();
	}
}
