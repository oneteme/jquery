package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.ArgumentParsers.jdbcArgParser;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.Validation;
import org.usf.jquery.core.ViewColumn;

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
	
	default TaggableColumn from(TableDecorator td) {
		var b = builder();
		return nonNull(b)
				? b.build(td).as(reference())
				: td.columnName(this) //recursive call
				.map(Validation::requireLegalVariable) //do it on init
				.map(cn-> new ViewColumn(td.table(), cn, reference(), dataType(td)))
				.orElseThrow(()-> undeclaredResouceException(td.identity(), identity()));
	}
	
	default JDBCArgumentParser parser(TableDecorator td){ // override parser | format | local
		return jdbcArgParser(dataType(td));
	}
	
	default JDBCType dataType(TableDecorator td) { // only if !builder
		return td.metadata().columnMetada(this)
		.map(ColumnMetadata::getDataType)
		.orElse(null);
	}
	
	default ColumnBuilder builder() { //set type if null
		return null; // no builder by default
	}
	
	default CriteriaBuilder<ComparisonExpression> criteria(String name) {
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
				return ref; // default column tag
			}
			
			@Override
			public ColumnBuilder builder() {
				return cb;
			}
		};
	}
}
