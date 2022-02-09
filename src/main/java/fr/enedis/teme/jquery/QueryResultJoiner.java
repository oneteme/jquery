package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class QueryResultJoiner implements DBQuery {

	private final JoinType joinType;
	private final RequestQuery request;
	
	@Override
	public void sql(StringBuilder sb, QueryColumnBuilder cb, QueryParameterBuilder pb, String alias) {
		
		sb.append(" ")
		.append(requireNonNull(joinType).toString())
		.append(" JOIN (");
		var sbc = new QueryColumnBuilder(cb.getSchema()); //specific ColumnBuilder
		request.build(sb, sbc, pb);
		sb.append(") ").append(alias);
		var src = new TableAlias(null, alias);
		List<Entry<TaggableColumn, DBTable>> com = new LinkedList<>();
		sbc.entries().forEach(e->{
			var o = cb.getEntry(e.getKey());
			if(o == null) {
				cb.appendColumn(e.getKey(), src);
			}
			else if(!request.getTable().equals(o.getValue())) { //column already added
				com.add(e);
			}
		});
		if(com.isEmpty()) {
			throw new RuntimeException("no join columns");
		}
		sb.append(" ON ").append(com.stream()
				.map(e-> alias + "." + e.getKey().tagname() + "=" + e.getValue().logicalColumnName(e.getKey()))
				.collect(AND.joiner()));
	}
}
