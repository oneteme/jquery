package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;
import static org.usf.jquery.core.Validation.requireAtMostNArgs;

/**
 * 
 * @author u$f
 *
 */
public interface DBTable extends DBObject {

	String reference(); //JSON & TAG
	
	String sql();
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {//schema, suffix ?
		requireAtMostNArgs(1, args, ()-> "DBTable " + reference());
		return isEmpty(args) ? sql() : args[0] + SPACE + sql();
	}
	
}
