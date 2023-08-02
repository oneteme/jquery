package org.usf.jquery.web;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchColumnException;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchTableException;

import java.util.Collection;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetadata {
	
	private final Map<String, TableMetadata> tables;

	@Deprecated
	public YearTableMetadata getYearTable(YearTableDecorator td){
		return (YearTableMetadata) tableMetada(td);
	}

	public TableMetadata tableMetada(TableDecorator td){
		return ofNullable(tables.get(td.identity()))
				.orElseThrow(()-> throwNoSuchTableException(td.identity()));
	}
	
	public ColumnMetadata columnMetada(TableDecorator td, ColumnDecorator cd){
		return ofNullable(tableMetada(td).getColumns().get(cd.identity()))
				.orElseThrow(()-> throwNoSuchColumnException(cd.identity())); //TODO change exception
	}

	public static DatabaseMetadata init(Collection<TableDecorator> tables, Collection<ColumnDecorator> columns) {
		return new DatabaseMetadata(tables.stream()
				.collect(toUnmodifiableMap(TableDecorator::identity, t-> t.createMetadata(columns))));
	}
	
	public static DatabaseMetadata emptyMetadata() {
		return new DatabaseMetadata(emptyMap());
	}
		
	
}

