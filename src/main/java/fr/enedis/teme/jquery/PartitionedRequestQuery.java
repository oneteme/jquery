package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBColumn.ofConstant;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;

import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PartitionedRequestQuery extends RequestQuery {
	
	private static final String REVISION_YEAR_TAG = "revisionYear";
	
	private final TaggableColumn revisionColumn;
	@Getter
	private final YearMonth[] revisions; // nullable
	
	private final Function<Integer, TaggableColumn[]> additionalColumns;
	
	public PartitionedRequestQuery(TaggableColumn revisionColumn, YearMonth... revisions) {
		this.revisionColumn = revisionColumn;
		this.revisions = revisions;
		if(isEmpty(revisions)) {
			this.noResult = true;
			this.additionalColumns = null; //unused
		}
		else {
			this.additionalColumns = v-> revisionColumn == null 
					? new TaggableColumn[] {ofConstant(v).as(REVISION_YEAR_TAG)}
					: new TaggableColumn[] {ofConstant(v).as(REVISION_YEAR_TAG), this.revisionColumn};
		}
	}
	
	@Override
	public List<TaggableColumn> getColumns() {
		if(isEmpty(revisions)) {
			return super.getColumns();
		}
		var list = new LinkedList<>(super.getColumns());
		list.addAll(asList(additionalColumns.apply(null))); //
		return list;
	}
	
	@Override
	public void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb){
		if(isEmpty(revisions)) {
			super.build(schema, sb, pb);
		}
		var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
		sb.forEach(map.entrySet(), " UNION ALL ", e-> query(e).build(schema, sb, pb));
	}
	
	private RequestQuery query(Entry<Integer, List<YearMonth>> entry) {
		var q = super.fork(tablename + "_" + entry.getKey())
				     .columns(additionalColumns.apply(entry.getKey()));
		if(revisionColumn == null) {
			return q;
		}
		return q.filters(entry.getValue().size() == 1 
				? revisionColumn.equal(entry.getValue().get(0).getMonthValue())
				: revisionColumn.in(entry.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new))); //int[] ?
	}
	
	@Override	
	public RequestQuery fork(String tn) {
		return new PartitionedRequestQuery(revisionColumn, revisions)
				.select(tn)
				.columns(columns.toArray(TaggableColumn[]::new))
				.filters(filters.toArray(DBFilter[]::new));
	}
	
}
