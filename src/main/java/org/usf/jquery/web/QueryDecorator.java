package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;

import java.util.Optional;

import org.usf.jquery.core.DBQuery;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.ViewColumn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class QueryDecorator implements TableDecorator {
	
	private final DBQuery query;
	
	@Override
	public String tableName() {
		return null;
	}
	
	@Override
	public String identity() {
		return query.id();
	}
	
	@Override
	public Optional<String> columnName(ColumnDecorator cd) {
		return column(cd.identity()).map(TaggableColumn::tagname);
	}
	
	public Optional<ColumnDecorator> lookupColumnDecorator(String cn) {
		return column(cn)
				.map(c-> ofColumn(cn, td-> new ViewColumn(table(), doubleQuote(c.tagname()), c.tagname(), c.getType())));
	}
	
	private final Optional<TaggableColumn> column(String cn){
		return query.columns().stream()
				.filter(c-> c.tagname().equals(cn))
				.findAny();
	}
	
	@Override
	public ViewBuilder builder() {
		return v-> query;
	}
}
