package org.usf.jquery.core;

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
public final class CaseColumnBuilder {

	private final DBColumn column;
	private final List<WhenCase> cases = new ArrayList<>();
	
	public CaseColumnBuilder() {
		this(null);
	}
	
	public CaseColumnBuilder when(Predicate exp, Object then) {
		if(nonNull(column)) {
			return when(column.filter(exp), then);
		}
		throw new IllegalArgumentException("cannot append expression " + exp);
	}
	
	public CaseColumnBuilder when(DBFilter filter, Object then) {
		cases.add(new WhenCase(filter, then));
		return this;
	}

	public CaseColumn orElse(Object o) {
		cases.add(new WhenCase(null, o));
		return end();
	}
	
	public CaseColumn end() {
		return new CaseColumn(cases.toArray(WhenCase[]::new));
	}
}
