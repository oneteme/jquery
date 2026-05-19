package org.usf.jquery.core;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class CaseColumnComposer implements Composer<CaseColumn> {

	private final Column column;
	private Collection<WhenCase> cases;
	
	public CaseColumnComposer() {
		this(null);
	}

	//column.when(predicate, then).when(predicate, then)...orElse(else)
	public CaseColumnComposer when(Predicate predicate, Object then) {
		if(nonNull(column)) {
			getCases().add(new WhenCase(column.filter(predicate), then));
			return this;
		}
		throw new IllegalArgumentException("cannot append predicate " + predicate);
	}
	
	//when(criteria, then).when(criteria, then)...orElse(else)
	public CaseColumnComposer when(Criteria criteria, Object then) {
		if(isNull(column)) {
			getCases().add(new WhenCase(criteria, then));
			return this;
		}
		throw new IllegalArgumentException("cannot append criteria " + criteria);
	}

	public CaseColumn orElse(Object o) {
		getCases().add(new WhenCase(null, o));
		return compose(null);
	}
	
	private Collection<WhenCase> getCases() {
		if(isNull(cases)) {
			cases = new ArrayList<>();
		}
		return cases;
	}
	
	@Override
	public CaseColumn compose(Store store) {
		if(!isEmpty(cases)) {			
			return new CaseColumn(unmodifiableCollection(cases));
		}
		throw new ComposeException("case column requires at least one when case");
	}
}