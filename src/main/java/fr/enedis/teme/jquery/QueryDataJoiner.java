package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class QueryDataJoiner implements DBQuery {

	private final JoinType joinType;
	private final RequestQuery request;
	private final JoinExpression joinExpression;

	@Override
	public void sql(StringBuilder sb, QueryColumnBuilder cb, QueryParameterBuilder pb, String alias) {
		
		sb.append(" ")
		.append(requireNonNull(joinType).toString())
		.append(" JOIN ")
		.append(requireNonNull(request).getTable().sql(cb.getSchema(), pb))
		.append(" ON ")
		.append(requireNonNull(joinExpression).sql(null, pb));
		for(var col : request.getColumns()) { //no query join 
			cb.appendColumn(col, request.getTable());
		}
	}
	
}
