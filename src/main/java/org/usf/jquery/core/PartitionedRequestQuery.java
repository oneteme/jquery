package org.usf.jquery.core;

import static java.util.Set.copyOf;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.concat;
import static org.usf.jquery.core.DBColumn.constant;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.Utils.isEmpty;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import lombok.Getter;

public final class PartitionedRequestQuery extends RequestQuery {
	
	private static final String REVISION_YEAR_TAG = "revisionYear";
	
	private final TaggableColumn revisionColumn;
	@Getter
	private final YearMonth[] revisions; // nullable
	
	public PartitionedRequestQuery(TaggableColumn revisionColumn, YearMonth... revisions) {
		this.revisionColumn = revisionColumn;
		this.revisions = revisions;
		if(isEmpty(revisions)) {
			this.noResult = true;
		}
	}
	
	@Override
	public final ParametredQuery build(String schema){
		
		var pb = parametrized();
		var sb = new SqlStringBuilder(500); // size ? 
		if(isEmpty(revisions)) {
			super.build(schema, sb, pb);
		}
		var cols = concat(columns.stream(), Stream.of(additionalColumns(null))) //only tagname
			.map(TaggableColumn::reference)
			.toArray(String[]::new);
		var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
		sb.forEach(map.entrySet(), " UNION ALL ", e-> query(e).build(schema, sb, pb)); //sequential 
		return new ParametredQuery(sb.toString(), cols, pb.args(), noResult);
	}
	
	private RequestQuery query(Entry<Integer, List<YearMonth>> entry) {
		
		var query = new RequestQuery(entry.getKey().toString(), copyOf(tables), new ArrayList<>(columns), new ArrayList<>(filters))
			     .columns(additionalColumns(entry.getKey()));
		if(revisionColumn == null) {
			return query;
		}
		return query.filters(entry.getValue().size() == 1 
				? revisionColumn.equal(entry.getValue().get(0).getMonthValue())
				: revisionColumn.in(entry.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new))); //int[] ?
	}
	
	private TaggableColumn[] additionalColumns(Integer year) {
		var yc = constant(year).as(REVISION_YEAR_TAG);
		return revisionColumn == null 
				? new TaggableColumn[] {yc} // year revision
				: new TaggableColumn[] {yc, this.revisionColumn};// year-month revision
	}
}
