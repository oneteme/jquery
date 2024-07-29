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
	public String columnName(ColumnDecorator cd) {
		return null;
	}
	
	@Override
	public DBView view() {
		return query;
	}
	
	@Override
	public TaggableColumn column(@NonNull ColumnDecorator cd) {
		return query.getQuery().getColumns().stream()
		.filter(c-> c.tagname().equals(cd.identity())) //tagname nullable !
		.findAny()
		.map(c-> new ViewColumn(query, c.tagname(), c.tagname(), c.getType()))
		.orElseThrow(()-> throwNoSuchColumnException(cd.identity()));
	}
	
	@Override
	public ViewMetadata metadata() {
		throw new UnsupportedOperationException("query metadata");
	}
}
