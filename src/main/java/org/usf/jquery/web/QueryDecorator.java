package org.usf.jquery.web;

import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryView;
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

	public NamedColumn column(String id) {
		return query.getBuilder().getColumns().stream() //do not use declaredColumn
		.filter(c-> id.equals(c.getTag()))
		.findAny()
		.map(c-> new ViewColumn(c.getTag(), query, c.getType(), null))
		.orElseThrow(()-> undeclaredResouceException(id, identity()));
	}
	
	@Override
	public String columnName(ColumnDecorator cd) {
		throw unsupportedOperationException("columnName");
	}
	
	@Override
	public NamedColumn column(@NonNull ColumnDecorator cd) {
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
		return new UnsupportedOperationException(identity() + "::" + method);
	}
}
