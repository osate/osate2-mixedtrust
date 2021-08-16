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
import org.osate.aadl2.contrib.aadlproject.TimeUnits;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.InstanceReferenceValue;
import org.osate.aadl2.properties.PropertyNotPresentException;
import org.osate.pluginsupport.properties.CodeGenUtil;
import org.osate.pluginsupport.properties.GeneratedRecord;
import org.osate.pluginsupport.properties.IntegerWithUnits;

public class MixedTrustTask extends GeneratedRecord {
	public static final String PERIOD__NAME = "Period";
	public static final String DEADLINE__NAME = "Deadline";
	public static final String GUESTTASK__NAME = "GuestTask";
	public static final String HYPERTASK__NAME = "HyperTask";
	public static final URI PERIOD__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties.aadl#/0/@ownedPropertyType.0/@ownedField.0");
	public static final URI DEADLINE__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties.aadl#/0/@ownedPropertyType.0/@ownedField.1");
	public static final URI GUESTTASK__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties.aadl#/0/@ownedPropertyType.0/@ownedField.2");
	public static final URI HYPERTASK__URI = URI.createURI("platform:/resource/mixedtrust/Mixed_Trust_Properties.aadl#/0/@ownedPropertyType.0/@ownedField.3");
	
	private final Optional<IntegerWithUnits<TimeUnits>> period;
	private final Optional<IntegerWithUnits<TimeUnits>> deadline;
	private final Optional<InstanceObject> guesttask;
	private final Optional<InstanceObject> hypertask;
	
	public MixedTrustTask(
			Optional<IntegerWithUnits<TimeUnits>> period,
			Optional<IntegerWithUnits<TimeUnits>> deadline,
			Optional<InstanceObject> guesttask,
			Optional<InstanceObject> hypertask
	) {
		this.period = period;
		this.deadline = deadline;
		this.guesttask = guesttask;
		this.hypertask = hypertask;
	}
	
	public MixedTrustTask(PropertyExpression propertyExpression, NamedElement lookupContext, Optional<Mode> mode) {
		RecordValue recordValue = (RecordValue) propertyExpression;
		
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
	
	@Override
	public RecordValue toPropertyExpression(ResourceSet resourceSet) {
		if (!period.isPresent()
				&& !deadline.isPresent()
				&& !guesttask.isPresent()
				&& !hypertask.isPresent()
		) {
			throw new IllegalStateException("Record must have at least one field set.");
		}
		RecordValue recordValue = Aadl2Factory.eINSTANCE.createRecordValue();
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
		return recordValue;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(
				period,
				deadline,
				guesttask,
				hypertask
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
		return Objects.equals(this.period, other.period)
				&& Objects.equals(this.deadline, other.deadline)
				&& Objects.equals(this.guesttask, other.guesttask)
				&& Objects.equals(this.hypertask, other.hypertask);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
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
		builder.append(']');
		return builder.toString();
	}
}
