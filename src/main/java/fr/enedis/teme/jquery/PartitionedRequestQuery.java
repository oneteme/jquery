package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static java.util.stream.Collectors.groupingBy;

import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PartitionedRequestQuery extends RequestQuery {
	
	private static final String REVISION_YEAR_TAG = "revisionYear";
	
	@NonNull
	private final YearMonth[] revisions;
	
	@Override
	List<String> columnTags() {
		if(revisions.length == 1) {
			return super.columnTags();
		}
		var list = new LinkedList<String>(super.columnTags());
		list.add(((YearPartitionTable) table).getRevisionColumn().tagname());
		if(Stream.of(revisions).map(YearMonth::getYear).distinct().count() > 1) {
			list.add(REVISION_YEAR_TAG);
		}
		return list;
	}
	
	@Override
	public void build(String schema, StringBuilder sb, QueryParameterBuilder pb){
		
		if(table instanceof YearPartitionTable == false) {
			super.build(schema, sb, pb);
		}
		else {
			var pTab = (YearPartitionTable) table;
			if(isEmpty(revisions)) {
				throw new IllegalArgumentException("missing parameter : " + pTab.getRevisionColumn().name().toLowerCase());
			}
			var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
			boolean mc = revisions.length > 1;
			boolean yc = map.size() > 1;
			var it = map.entrySet().iterator();
			query(it.next(), pTab.getRevisionColumn(), mc, yc).build(schema, sb, pb);
			while(it.hasNext()) {
				sb.append(" UNION ");
				query(it.next(), pTab.getRevisionColumn(), mc, yc).build(schema, sb, pb);
			}
		}
	}
	
	private RequestQuery query(Entry<Integer, List<YearMonth>> entry, TableColumn revisionColumn, boolean mc, boolean yc) {

		var filter = entry.getValue().size() == 1 
				? revisionColumn.equal(entry.getValue().get(0).getMonthValue())
				: revisionColumn.in(entry.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new));
		var req = super.fork(new TableAdapter(table, entry.getKey() + ""));
		req.filters(filter);
		req.column(mc, ()-> revisionColumn);
		req.column(yc, ()-> staticColumn(REVISION_YEAR_TAG, entry.getKey()));
		return req;
	}
	
	@Override
	public RequestQuery fork(DBTable tab) {
		return new PartitionedRequestQuery(revisions)
				.select(tab)
				.columns(columns.toArray(TaggableColumn[]::new))
				.filters(filters.toArray(DBFilter[]::new));
	}
	
	@Override
	int estimateSize() {
		var n = (int)Stream.of(revisions).map(YearMonth::getYear).distinct().count();
		return 1000 * (resultJoins.size()+n);
	}
}
