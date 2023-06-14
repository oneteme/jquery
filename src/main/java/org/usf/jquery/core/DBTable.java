package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

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
		illegalArgumentIf(hasSize(args, s-> s > 1), "table takes at most one parameter");
		return isEmpty(args) ? sql() : args[0] + SPACE + sql();
	}
	
}
