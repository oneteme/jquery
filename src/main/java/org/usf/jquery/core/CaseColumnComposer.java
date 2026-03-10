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
public final class CaseColumnComposer implements Composer<CaseColumn> {

	private final Column column;
	private final List<WhenCase> cases = new ArrayList<>();
	
	public CaseColumnComposer() {
		this(null);
	}
	
	public CaseColumnComposer when(Predicate predicate, Object then) {
		if(nonNull(column)) {
			return when(column.filter(predicate), then);
		}
		throw new IllegalArgumentException("cannot append expression " + predicate);
	}
	
	public CaseColumnComposer when(Criteria criteria, Object then) {
		cases.add(new WhenCase(criteria, then));
		return this;
	}

	public CaseColumn orElse(Object o) {
		cases.add(new WhenCase(null, o));
		return compose();
	}
	
	@Override
	public CaseColumn compose() {
		return new CaseColumn(cases.toArray(WhenCase[]::new));
	}
}
