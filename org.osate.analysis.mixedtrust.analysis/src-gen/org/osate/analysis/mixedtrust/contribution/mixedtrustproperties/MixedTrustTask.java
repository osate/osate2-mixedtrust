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
import org.osate.aadl2.StringLiteral;
import org.osate.aadl2.contrib.aadlproject.TimeUnits;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.InstanceReferenceValue;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.pluginsupport.properties.CodeGenUtil;
import org.osate.pluginsupport.properties.GeneratedRecord;
import org.osate.pluginsupport.properties.IntegerWithUnits;

public class MixedTrustTask extends GeneratedRecord {
	public static final String NAME__NAME = "Name";
	public static final String PERIOD__NAME = "Period";
	public static final String DEADLINE__NAME = "Deadline";
	public static final String GUESTTASK__NAME = "GuestTask";
	public static final String HYPERTASK__NAME = "HyperTask";
	public static final String E__NAME = "E";
	public static final URI NAME__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.0/@ownedField.0");
	public static final URI PERIOD__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.0/@ownedField.1");
	public static final URI DEADLINE__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.0/@ownedField.2");
	public static final URI GUESTTASK__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.0/@ownedField.3");
	public static final URI HYPERTASK__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.0/@ownedField.4");
	public static final URI E__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties_set.aadl#/0/@ownedPropertyType.0/@ownedField.5");

	private final Optional<String> name;
	private final Optional<IntegerWithUnits<TimeUnits>> period;
	private final Optional<IntegerWithUnits<TimeUnits>> deadline;
	private final Optional<InstanceObject> guesttask;
	private final Optional<InstanceObject> hypertask;
	private final Optional<IntegerWithUnits<TimeUnits>> e;

	public MixedTrustTask(
			Optional<String> name,
			Optional<IntegerWithUnits<TimeUnits>> period,
			Optional<IntegerWithUnits<TimeUnits>> deadline,
			Optional<InstanceObject> guesttask,
			Optional<InstanceObject> hypertask,
			Optional<IntegerWithUnits<TimeUnits>> e
	) {
		this.name = name;
		this.period = period;
		this.deadline = deadline;
		this.guesttask = guesttask;
		this.hypertask = hypertask;
		this.e = e;
	}

	public MixedTrustTask(PropertyExpression propertyExpression, NamedElement lookupContext, Optional<Mode> mode) {
		RecordValue recordValue = (RecordValue) propertyExpression;

		Optional<String> name_local;
		try {
			name_local = findFieldValue(recordValue, NAME__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return ((StringLiteral) resolved).getValue();
			});
		} catch (PropertyNotPresentException e) {
			name_local = Optional.empty();
		}
		this.name = name_local;

		Optional<IntegerWithUnits<TimeUnits>> period_local;
		try {
			period_local = findFieldValue(recordValue, PERIOD__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return new IntegerWithUnits<>(resolved, TimeUnits.class);
			});
		} catch (PropertyNotPresentException e) {
			period_local = Optional.empty();
		}
		this.period = period_local;

		Optional<IntegerWithUnits<TimeUnits>> deadline_local;
		try {
			deadline_local = findFieldValue(recordValue, DEADLINE__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return new IntegerWithUnits<>(resolved, TimeUnits.class);
			});
		} catch (PropertyNotPresentException e) {
			deadline_local = Optional.empty();
		}
		this.deadline = deadline_local;

		Optional<InstanceObject> guesttask_local;
		try {
			guesttask_local = findFieldValue(recordValue, GUESTTASK__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return ((InstanceReferenceValue) resolved).getReferencedInstanceObject();
			});
		} catch (PropertyNotPresentException e) {
			guesttask_local = Optional.empty();
		}
		this.guesttask = guesttask_local;

		Optional<InstanceObject> hypertask_local;
		try {
			hypertask_local = findFieldValue(recordValue, HYPERTASK__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return ((InstanceReferenceValue) resolved).getReferencedInstanceObject();
			});
		} catch (PropertyNotPresentException e) {
			hypertask_local = Optional.empty();
		}
		this.hypertask = hypertask_local;

		Optional<IntegerWithUnits<TimeUnits>> e_local;
		try {
			e_local = findFieldValue(recordValue, E__NAME).map(field -> {
				PropertyExpression resolved = CodeGenUtil.resolveNamedValue(field.getOwnedValue(), lookupContext, mode);
				return new IntegerWithUnits<>(resolved, TimeUnits.class);
			});
		} catch (PropertyNotPresentException e) {
			e_local = Optional.empty();
		}
		this.e = e_local;
	}

	public Optional<String> getName() {
		return name;
	}

	public Optional<IntegerWithUnits<TimeUnits>> getPeriod() {
		return period;
	}

	public Optional<IntegerWithUnits<TimeUnits>> getDeadline() {
		return deadline;
	}

	public Optional<InstanceObject> getGuesttask() {
		return guesttask;
	}

	public Optional<InstanceObject> getHypertask() {
		return hypertask;
	}

	public Optional<IntegerWithUnits<TimeUnits>> getE() {
		return e;
	}

	@Override
	public RecordValue toPropertyExpression(ResourceSet resourceSet) {
		if (!name.isPresent()
				&& !period.isPresent()
				&& !deadline.isPresent()
				&& !guesttask.isPresent()
				&& !hypertask.isPresent()
				&& !e.isPresent()
		) {
			throw new IllegalStateException("Record must have at least one field set.");
		}
		RecordValue recordValue = Aadl2Factory.eINSTANCE.createRecordValue();
		name.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, NAME__URI, NAME__NAME));
			fieldAssociation.setOwnedValue(CodeGenUtil.toPropertyExpression(field));
		});
		period.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, PERIOD__URI, PERIOD__NAME));
			fieldAssociation.setOwnedValue(field.toPropertyExpression(resourceSet));
		});
		deadline.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, DEADLINE__URI, DEADLINE__NAME));
			fieldAssociation.setOwnedValue(field.toPropertyExpression(resourceSet));
		});
		guesttask.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, GUESTTASK__URI, GUESTTASK__NAME));
			fieldAssociation.setOwnedValue(CodeGenUtil.toPropertyExpression(field));
		});
		hypertask.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, HYPERTASK__URI, HYPERTASK__NAME));
			fieldAssociation.setOwnedValue(CodeGenUtil.toPropertyExpression(field));
		});
		e.ifPresent(field -> {
			BasicPropertyAssociation fieldAssociation = recordValue.createOwnedFieldValue();
			fieldAssociation.setProperty(loadField(resourceSet, E__URI, E__NAME));
			fieldAssociation.setOwnedValue(field.toPropertyExpression(resourceSet));
		});
		return recordValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				name,
				period,
				deadline,
				guesttask,
				hypertask,
				e
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MixedTrustTask)) {
			return false;
		}
		MixedTrustTask other = (MixedTrustTask) obj;
		return Objects.equals(this.name, other.name)
				&& Objects.equals(this.period, other.period)
				&& Objects.equals(this.deadline, other.deadline)
				&& Objects.equals(this.guesttask, other.guesttask)
				&& Objects.equals(this.hypertask, other.hypertask)
				&& Objects.equals(this.e, other.e);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		this.name.ifPresent(field -> {
			builder.append(NAME__NAME);
			builder.append(" => \"");
			builder.append(field);
			builder.append("\";");
		});
		this.period.ifPresent(field -> {
			builder.append(PERIOD__NAME);
			builder.append(" => ");
			builder.append(field);
			builder.append(';');
		});
		this.deadline.ifPresent(field -> {
			builder.append(DEADLINE__NAME);
			builder.append(" => ");
			builder.append(field);
			builder.append(';');
		});
		this.guesttask.ifPresent(field -> {
			builder.append(GUESTTASK__NAME);
			builder.append(" => reference (");
			builder.append(field.getName());
			builder.append(");");
		});
		this.hypertask.ifPresent(field -> {
			builder.append(HYPERTASK__NAME);
			builder.append(" => reference (");
			builder.append(field.getName());
			builder.append(");");
		});
		this.e.ifPresent(field -> {
			builder.append(E__NAME);
			builder.append(" => ");
			builder.append(field);
			builder.append(';');
		});
		builder.append(']');
		return builder.toString();
	}
}
