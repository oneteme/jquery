package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.Comparator.equal;
import static org.usf.jquery.core.Comparator.greaterOrEqual;
import static org.usf.jquery.core.Comparator.greaterThan;
import static org.usf.jquery.core.Comparator.iLike;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.Comparator.lessOrEqual;
import static org.usf.jquery.core.Comparator.lessThan;
import static org.usf.jquery.core.Comparator.like;
import static org.usf.jquery.core.Comparator.notEqual;
import static org.usf.jquery.core.Comparator.notIn;
import static org.usf.jquery.core.Comparator.notLike;
import static org.usf.jquery.web.ArgumentParsers.jdbcArgParser;

import org.usf.jquery.core.Comparator;
import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.StringComparator;

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
	
	/**
	 * override parser | format | local
	 */
	default JDBCArgumentParser parser(TableDecorator td){
		return jdbcArgParser(dataType(td));
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
	
	default ColumnBuilder builder() {
		return null; // physical column by default
	}
	
	default CriteriaBuilder<String> criteria(String name) {
		return null; // no criteria by default
	}

	default Comparator comparator(String comparator, int nArg) {
		if(isNull(comparator)) {
			return nArg == 1 ? equal() : in();
		}
		switch(comparator) {
		case "gt"		: return greaterThan();
		case "ge"  		: return greaterOrEqual();
		case "lt"  		: return lessThan();
		case "le"  		: return lessOrEqual();
		case "not" 		: return nArg == 1 ? notEqual() : notIn();
		case "like"		: return wildcards(like());
		case "ilike"	: return wildcards(iLike());
		case "unlike"	: return wildcards(notLike());
		default			: return null;
		//isnull
		}
	}
	
	private static Comparator wildcards(StringComparator fn) {
		return (b, args)-> {
			args[1] = "%" + args[1] + "%";
			return fn.sql(b, args);
		};
	}
	
	default ComparisonExpression expression(String exp, String... values) { return null; }
	
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
