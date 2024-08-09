package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBView extends DBObject {
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBView.class::getSimpleName);
		return sql(builder);
	}

	String sql(QueryParameterBuilder builder);
	
	default NamedView as(String tag) {
		return new NamedView(this, isNull(tag) ? null : requireLegalVariable(tag));
	}
	
	default String sqlWithTag(QueryParameterBuilder builder) {
		return sql(builder) + SPACE + builder.view(this);
	}
}
