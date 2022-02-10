package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
final class QueryResultJoiner {

	private final JoinType joinType;
	private final RequestQuery request;
	private final String[] columns;
	
	private String alias;
	
	public void sql(String schema, StringBuilder sb, QueryParameterBuilder pb, Map<String, String> columnMap) {
		
		Predicate<String> filter = t-> {
			var e = columnMap.get(t);
			return e != null && !e.equals(alias);
		};
		Stream<String> col = isEmpty(columns)
				? request.columnTags().stream().filter(filter)
				: Stream.of(columns);
		String[] criteria = col.map(c-> alias + "." + c + "=" + requireNonNull(columnMap.get(c)) + "." + c)
				.toArray(String[]::new);
		sb.append(requireNonNull(joinType).toString())
		.append(" JOIN (");
		request.build(schema, sb, pb);
		sb.append(") ").append(alias);
		sb.append(" ON ").append(AND.join(requireNonEmpty(criteria)));
	}
}
