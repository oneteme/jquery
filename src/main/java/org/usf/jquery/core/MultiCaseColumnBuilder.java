package org.usf.jquery.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author u$f
 *
 */
public final class MultiCaseColumnBuilder {

	private final List<WhenCase> cases = new ArrayList<>();
	
	public MultiCaseColumnBuilder when(DBFilter filter, Object then) {
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
