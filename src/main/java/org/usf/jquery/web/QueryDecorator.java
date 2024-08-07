package org.usf.jquery.web;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.ViewColumn;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
final class QueryDecorator implements ViewDecorator {
	
	private final String id;
	private final QueryView query;

	@Override
	public String identity() {	
		return id;
	}
	
	@Override
	public DBView view() {
		return query;
	}

	public TaggableColumn column(String id) {
		return query.getBuilder().getColumns().stream()
		.filter(c-> c.tagname().equals(id)) //tagname nullable !
		.findAny()
		.map(c-> new ViewColumn(query, c.tagname(), c.tagname(), c.getType()))
		.orElse(null);
	}
	
	@Override
	public String columnName(ColumnDecorator cd) {
		throw unsupportedOperationException("columnName");
	}
	
	@Override
	public TaggableColumn column(@NonNull ColumnDecorator cd) {
		throw unsupportedOperationException("column");
	}
	
	@Override
	public ViewBuilder builder() {
		throw unsupportedOperationException("builder");
	}
	
	@Override
	public ViewMetadata metadata() {
		throw unsupportedOperationException("metadata");
	}
	
	private UnsupportedOperationException unsupportedOperationException(String method) {
		return new UnsupportedOperationException(identity() + "." + method);
	}
}
