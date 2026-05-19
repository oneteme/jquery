package org.usf.jquery.core;

import static java.util.Arrays.asList;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class JoinsClause implements DBObject {

	private final Collection<ViewJoin> joins;
	
	public JoinsClause(ViewJoin... joins) {
		if(isEmpty(joins)) {
			throw new ComposeException("JoinsClause requires at least one join");
		}
		this.joins = asList(joins);
	}

	@Override
	public int prepare(QueryManifest declare) {
		return declare.prepareNested(joins);
	}

	@Override
	public void build(QueryBuilder builder, Object... args) {
		builder.appendEach(SPACE, joins);
	}
	
	public static JoinsClause joins(ViewJoin... joins) {
		return new JoinsClause(joins);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
