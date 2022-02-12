package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.SqlStringBuilder.COMA_SEPARATOR;
import static fr.enedis.teme.jquery.SqlStringBuilder.SPACE_SEPARATOR;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class QueryResultJoiner implements Query {

	private final JoinType joinType;
	private final RequestQuery request;
	
	private String alias;
	private Collection<Supplier<String>> criteria;
	
	@Override
	public void columns(String alias, SqlStringBuilder sb, QueryParameterBuilder pb, Map<String, String> columnMap) {

		this.alias = alias;
		this.criteria = new LinkedList<>();
		var tc = new LinkedList<String>();
		var simple = request.isSimpleQuery();
		for(var c : request.getColumns()) {
			var rs = columnMap.get(c.tagname());
			if(rs == null) {
				columnMap.put(c.tagname(), alias);
				tc.add(alias + "." + (simple ? c.tagSql(request.getTable(), pb) : c.tagname()));
			}
			else {
				var cn = simple ? c.sql(request.getTable(), pb) : c.tagname();
				criteria.add(()-> alias + "." + cn + "=" + rs + "." + c.tagname());
			}
		}
		sb.append(String.join(COMA_SEPARATOR, tc.toArray(String[]::new)));
	}

	@Override
	public void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb) {

		sb.append(requireNonNull(joinType).sql())
		.append("JOIN ");
		if(request.isSimpleQuery()) {
			sb.append(request.getTable().sql(schema, pb));
		}
		else {
			sb.append("(");
			request.build(schema, sb, pb);
			sb.append(")");
		}
		sb.append(SPACE_SEPARATOR)
		.append(alias)
		.append(" ON ")
		.appendEach(requireNonEmpty(criteria), AND.sql(), Supplier::get);
	}
	
}
