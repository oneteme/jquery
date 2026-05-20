package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;
import java.util.Objects;

/**
 * 
 * @author u$f
 *
 */
public final class CaseColumn implements Column {

	private final Collection<WhenCase> cases;

	public CaseColumn(Collection<WhenCase> cases) {
		if(isEmpty(cases)) {			
			throw new ComposeException("CaseColumn requires at least one when case");
		}
		this.cases = cases;
	}
	
	@Override
	public int prepare(QueryAnalyzer analyzer) {
		return analyzer.analyzeNested(cases, this);
	}

	@Override
	public void build(SqlBuilder builder) {
		builder.withValue()
		.append("CASE ").appendEach(SPACE, cases).append(" END");
	}
	
	@Override
	public JDBCType getType() {
		return cases.stream()
				.map(WhenCase::getType)
				.filter(Objects::nonNull) // should have same type
				.findAny().orElse(null);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
