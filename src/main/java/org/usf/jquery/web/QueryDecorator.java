package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;

import java.util.Map;
import java.util.Optional;

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

	public Optional<NamedColumn> column(String id) {
		return query.getComposer().getColumns().stream() //do not use declaredColumn
		.filter(c-> id.equals(c.getTag()))
		.findAny()
		.map(c-> new ViewColumn(doubleQuote(c.getTag()), query, c.getType(), null));
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
	public ViewMetadata metadata(Map<String, ColumnMetadata> colMetadata) {
		throw unsupportedOperationException("metadata");
	}
	
	private UnsupportedOperationException unsupportedOperationException(String method) {
		return new UnsupportedOperationException(identity() + "::" + method);
	}
}
