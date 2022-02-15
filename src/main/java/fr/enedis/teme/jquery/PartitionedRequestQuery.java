package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;

import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PartitionedRequestQuery extends RequestQuery {
	
	private static final String REVISION_YEAR_TAG = "revisionYear";
	
	@NonNull
	private final YearMonth[] revisions;
	
	private final Function<Integer, TaggableColumn[]> revisionColumns;
	
	public PartitionedRequestQuery(@NonNull YearMonth[] revisions) {
		this.revisions = revisions;
		if(revisions.length ==1 ) {
			revisionColumns = v-> new TaggableColumn[] {};
		}
		else if(Stream.of(revisions).map(YearMonth::getYear).distinct().count() > 1) {
			revisionColumns = v-> new TaggableColumn[] {
				((YearPartitionTable) table).getRevisionColumn(),
				staticColumn(REVISION_YEAR_TAG, v)
			};
		}
		else {
			revisionColumns = v-> new TaggableColumn[] {
				((YearPartitionTable) table).getRevisionColumn()
			};
		}
	}
	
	@Override
	public List<TaggableColumn> getColumns() {
		var add = revisionColumns.apply(null);
		if(add.length == 0) {
			return super.getColumns();
		}
		var list = new LinkedList<>(super.getColumns());
		list.addAll(asList(add));
		return list;
	}
	
	@Override
	public void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
		
		if(table instanceof YearPartitionTable == false) {
			super.build(schema, sb, pb);
		}
		else {
			var pTab = (YearPartitionTable) table;
			if(isEmpty(revisions)) {
				throw new IllegalArgumentException("missing revision parameter");
			}
			var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
			sb.forEach(map.entrySet(), " UNION ", e-> query(e, pTab.getRevisionColumn()).build(schema, sb, pb));
		}
	}
	
	private RequestQuery query(Entry<Integer, List<YearMonth>> entry, TableColumn revisionColumn) {

		return super.fork(new TableAdapter(table, entry.getKey() + ""), true)
				.columns(revisionColumns.apply(entry.getKey()))
				.filters(entry.getValue().size() == 1 
				? revisionColumn.equal(entry.getValue().get(0).getMonthValue())
				: revisionColumn.in(entry.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)));
	}
	
	@Override	
	public RequestQuery fork(DBTable tab, boolean joins) {
		var q = new PartitionedRequestQuery(revisions)
				.select(tab)
				.columns(columns.toArray(TaggableColumn[]::new))
				.filters(filters.toArray(DBFilter[]::new));
		if(joins) {// change this => constructor
			q.resultJoins = this.resultJoins;
		}
		return q;
	}
	
	@Override
	int estimateSize() {
		var n = (int)Stream.of(revisions).map(YearMonth::getYear).distinct().count();
		return 500 * (resultJoins.size()+n);
	}
	
	@Override
	public boolean isSimpleQuery() {
		return false; //TODO check that
	}
}
