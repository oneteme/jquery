package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class CaseColumnComposer implements Composer<CaseColumn> {

	private final Column column;
	private final List<WhenCase> cases = new ArrayList<>(); //required at least one case
	
	public CaseColumnComposer() {
		this(null);
	}

	//column.when(predicate, then).when(predicate, then)...orElse(else)
	public CaseColumnComposer when(Predicate predicate, Object then) {
		if(nonNull(column)) {
			cases.add(new WhenCase(column.filter(predicate), then));
			return this;
		}
		throw new IllegalArgumentException("cannot append predicate " + predicate);
	}
	
	//when(criteria, then).when(criteria, then)...orElse(else)
	public CaseColumnComposer when(Criteria criteria, Object then) {
		if(isNull(column)) {
			cases.add(new WhenCase(criteria, then));
			return this;
		}
		throw new IllegalArgumentException("cannot append criteria " + criteria);
	}

	public CaseColumn orElse(Object o) {
		cases.add(new WhenCase(null, o));
		return compose();
	}
	
	@Override
	public CaseColumn compose() {
		if(!cases.isEmpty()) {			
			return new CaseColumn(cases.toArray(WhenCase[]::new));
		}
		throw new IllegalArgumentException("");
	}
}