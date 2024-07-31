package org.usf.jquery.web;

import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchColumnException;

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
	
	private final QueryView query;

	@Override
	public String identity() {	
		return query.id();
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
		.orElseThrow(()-> throwNoSuchColumnException(id));
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
	
	UnsupportedOperationException unsupportedOperationException(String method) {
		return new UnsupportedOperationException(this.getClass().getSimpleName() + "." + method);
	}
}
