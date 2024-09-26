package org.usf.jquery.core;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class SingleCaseColumnBuilder {

	private final DBColumn column;
	private final List<WhenCase> cases = new ArrayList<>();
	
	public SingleCaseColumnBuilder when(ComparisonExpression exp, Object then) {
		cases.add(new WhenCase(column.filter(exp), then));
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
