package org.usf.jquery.web;

import static org.usf.jquery.web.ArgumentParsers.jdbcArgParser;

import org.usf.jquery.core.JDBCType;

/**
 * 
 * @author u$f
 * 
 *
 */
@FunctionalInterface
public interface ColumnDecorator {
	
	String identity();  //URL
	
	default String reference() { //JSON
		return identity();
	}
	
	default JDBCType dataType(TableDecorator td) {
		return td.metadata().columnMetada(this)
		.map(ColumnMetadata::getDataType)
		.orElse(null); 
	}
	
	default JDBCArgumentParser parser(TableDecorator td){ // override parser | format | local
		return jdbcArgParser(dataType(td));
	}
	
	default ColumnBuilder builder() {
		return null; // physical column by default
	}
	
	default CriteriaBuilder<String> criteria(String name) { 
		return null;  // no criteria by default
	}
	
	default String pattern(TableDecorator td) {
		throw new UnsupportedOperationException(); //improve API security and performance
	}

	default boolean canSelect(TableDecorator td) {
		throw new UnsupportedOperationException(); //authorization inject
	}

	default boolean canFilter(TableDecorator td) {
		throw new UnsupportedOperationException(); //authorization inject
	}
	
	static ColumnDecorator ofColumn(String ref, ColumnBuilder cb) {
		return new ColumnDecorator() {
			@Override
			public String identity() {
				return null; //unused
			}
			@Override
			public String reference() {
				return ref;
			}
			@Override
			public ColumnBuilder builder() {
				return cb;
			}
		};
	}
}
