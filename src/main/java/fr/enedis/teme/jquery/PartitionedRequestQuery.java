package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static java.util.Arrays.copyOf;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.time.YearMonth;
import java.util.Collection;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PartitionedRequestQuery extends RequestQuery {
	
	private final YearMonth[] revisions;
	
	@Override
	public ParametredQuery build(String schema){
		
		if(table instanceof YearPartitionTable == false) {
			return super.build(schema);
		}
		var pTab = (YearPartitionTable) table;
		if(isEmpty(revisions)) {
			throw new IllegalArgumentException("missing parameter : " + pTab.getRevisionColumn().name().toLowerCase());
		}
		var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
		if(map.size() == 1) {//one table reference
			var e = map.entrySet().iterator().next();
			filters(pTab.getRevisionColumn().in(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)));
			if(e.getValue().size() > 1) {//add month rev. when multiple values
				columns(pTab.getRevisionColumn());
			}
			return super.build(schema, e.getKey().toString());
		}
		var queries = map.entrySet().stream()
			.map(e-> {
				var ftrs = new DBFilter[]{pTab.getRevisionColumn().in(e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new))}; //TD to int
				var cols = new TaggableColumn[]{pTab.getRevisionColumn(), staticColumn("revisionYear", e.getKey())}; //add year rev. when multiple values
				return new RequestQuery()
						.select(table, concat(this.columns, cols))
						.filters(concat(this.filters, ftrs))
						.build(schema, e.getKey().toString());
			})
			.collect(toList());
		return join(queries);
	}
	
	@Override
	public RequestQuery fork(DBTable tab) {
		return new PartitionedRequestQuery(revisions)
				.select(tab)
				.columns(copyOf(columns, columns.length))
				.filters(copyOf(filters, filters.length));
	}
	
	//TD impl. collector
	private static final ParametredQuery join(Collection<ParametredQuery> queries) {
		requireNonEmpty(queries);
		//check columns ?
		return new ParametredQuery(
				queries.stream().map(ParametredQuery::getQuery).collect(joining(" UNION ")),
				queries.iterator().next().getColumnNames(),
				queries.stream().flatMap(o-> Stream.of(o.getParams())).toArray());
	}
}
