package org.usf.jquery.web;

import java.util.Map;
import java.util.Optional;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Query;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class QueryDecorator implements ViewDecorator {
	
	private final String id;
	@Getter
	private final Query query; //unmodifiable

	@Override
	public String identity() {	
		return id;
	}
	
	@Override
	public Query view() {
		return query; //do not use env cache
	}

	public Optional<Column> column(String id) {
		return null;
	}
	
	@Override
	public String columnName(ColumnDecorator cd) {
		throw unsupportedOperationException("columnName");
	}
	
	@Override
	public Column column(@NonNull ColumnDecorator cd, String... args) {
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
