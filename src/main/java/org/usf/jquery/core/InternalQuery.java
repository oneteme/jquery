package org.usf.jquery.core;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.space;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class InternalQuery implements Query {//see RequestQueryBuilder::build
	
	private final DBColumn[] columns;
	private final DBFilter[] filters;

	@Override
	public String sql(QueryParameterBuilder builder) {
		var sub = builder.subQuery(); 
		var sb = new SqlStringBuilder(100)
				.append("SELECT ").append(sub.appendLitteralArray(columns))
				.append(" FROM ").appendEach(sub.views(), SCOMA, v-> v.sqlWithTag(sub));
    	filter(sub, sb, "WHERE", not(DBFilter::isAggregation));
//    	filter(sub, sb, "HAVING", DBFilter::isAggregation);
		return sb.toString();
	}
	
	void filter(QueryParameterBuilder pb, SqlStringBuilder sb, String caluse, Predicate<DBFilter> pre){
		if(!isEmpty(filters)) {
			var ex = Stream.of(filters)
					.filter(pre)
					.map(f-> f.sql(pb))
					.collect(joining(AND.sql()));
			if(!ex.isEmpty()) {
				sb.append(space(caluse)).append(ex);
			}
		}
	}

	@Override
	public String toString() {
		return sql(addWithValue());
	}

}
