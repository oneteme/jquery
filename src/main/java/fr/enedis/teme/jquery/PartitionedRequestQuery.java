package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBColumn.ofConstant;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;

import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PartitionedRequestQuery extends RequestQuery {
	
	private static final String REVISION_YEAR_TAG = "revisionYear";
	
	@Getter
	private final YearMonth[] revisions; // nullable
	
	private final BiFunction<YearPartitionTable, Integer, TaggableColumn[]> revisionColumns;
	
	public PartitionedRequestQuery(YearMonth[] revisions) {
		this.revisions = revisions;
		this.revisionColumns = (t, v)-> t.revisionColumn() == null 
			? new TaggableColumn[] {ofConstant(v).as(REVISION_YEAR_TAG)}
			: new TaggableColumn[] {ofConstant(v).as(REVISION_YEAR_TAG), t.revisionColumn()};
		this.noResult = isEmpty(revisions);
	}
	
	@Override
	public List<TaggableColumn> getColumns() {
		if(isEmpty(revisions)) {
			return super.getColumns();
		}
		if(table instanceof YearPartitionTable) {
			var list = new LinkedList<>(super.getColumns());
			list.addAll(asList(revisionColumns.apply((YearPartitionTable) table, null))); //
			return list;
		}
		throw new IllegalArgumentException(table + " is not YearPartitionTable");
	}
	
	@Override
	public void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
		if(isEmpty(revisions)) {
			super.build(schema, sb, pb);
		}
		else if(table instanceof YearPartitionTable) {
			var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
			var tab = (YearPartitionTable) table;
			sb.forEach(map.entrySet(), " UNION ALL ", e-> query(tab, e).build(schema, sb, pb));
		}
		else {
			throw new IllegalArgumentException(table + " is not YearPartitionTable");
		}
	}
	
	private RequestQuery query(YearPartitionTable tab, Entry<Integer, List<YearMonth>> entry) {
		var q = super.fork(tab.suffix(entry.getKey() + ""))
				     .columns(revisionColumns.apply(tab, entry.getKey()));
		if(tab.revisionColumn() == null) {
			return q;
		}
		return q.filters(entry.getValue().size() == 1 
				? tab.revisionColumn().equal(entry.getValue().get(0).getMonthValue())
				: tab.revisionColumn().in(entry.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)));
	}
	
	@Override	
	public RequestQuery fork(DBTable tab) {
		return new PartitionedRequestQuery(revisions)
				.select(tab)
				.columns(columns.toArray(TaggableColumn[]::new))
				.filters(filters.toArray(DBFilter[]::new));
	}
	
}
