package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.toList;

import java.util.Collection;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class JoinGroup implements QueryPart {

	private final Collection<Join> joins;
	
	public JoinGroup(Join... joins) {
		if(isEmpty(joins)) {
			throw new ComposeException("JoinsClause requires at least one join");
		}
		this.joins = toList(joins);
	}

	@Override
	public int prepare(QueryAnalyzer declare) {
		return declare.analyzeNested(joins);
	}

	@Override
	public void build(SqlBuilder builder, Object... args) {
		builder.appendEach(SqlBuilder.SPACE, joins);
	}
	
	public static JoinGroup joins(Join... joins) {
		return new JoinGroup(joins);
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
