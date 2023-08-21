package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.DBColumn.count;
import static org.usf.jquery.core.DBComparator.equal;
import static org.usf.jquery.core.DBComparator.greaterOrEqual;
import static org.usf.jquery.core.DBComparator.greaterThan;
import static org.usf.jquery.core.DBComparator.iLike;
import static org.usf.jquery.core.DBComparator.in;
import static org.usf.jquery.core.DBComparator.lessOrEqual;
import static org.usf.jquery.core.DBComparator.lessThan;
import static org.usf.jquery.core.DBComparator.like;
import static org.usf.jquery.core.DBComparator.notEqual;
import static org.usf.jquery.core.DBComparator.notIn;
import static org.usf.jquery.core.DBComparator.notLike;
import static org.usf.jquery.core.JDBCType.AUTO_TYPE;
import static org.usf.jquery.web.ParsableJDBCType.typeOf;

import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.DBFunction;
import org.usf.jquery.core.SQLType;
import org.usf.jquery.core.StringComparator;

/**
 * 
 * @author u$f
 * 
 * @see TableDecorator
 * @see RequestColumn
 *
 */
public interface ColumnDecorator {
	
	String identity();  //URL
	
	String reference(); //JSON
	
	default SQLType dataType() {
		return AUTO_TYPE;
	}
	
	default String pattern() {
		throw new UnsupportedOperationException(); //improve API security and performance
	}

	default boolean canSelect() {
		throw new UnsupportedOperationException(); //authorization inject
	}

	default boolean canFilter() {
		throw new UnsupportedOperationException(); //authorization inject
	}
	
	default ColumnBuilder builder() {
		return null; // physical column by default
	}
	
	default CriteriaBuilder<String> criteria(String name) {
		return null; // no criteria by default
	}

	default DBComparator comparator(String comparator, int nArg) {
		if(isNull(comparator)) {
			return nArg == 1 ? equal() : in();
		}
		switch(comparator) {
		case "gt"		: return greaterThan();
		case "ge"  		: return greaterOrEqual();
		case "lt"  		: return lessThan();
		case "le"  		: return lessOrEqual();
		case "not" 		: return nArg == 1 ? notEqual() : notIn();
		case "like"		: return containsArgPartten(like());
		case "ilike"	: return containsArgPartten(iLike());
		case "unlike"	: return containsArgPartten(notLike());
		default			: return null;
		//isnull
		}
	}
	
	/**
	 * override parser | format | local
	 */
	default ArgumentParser parser(SQLType type){
		return type instanceof ParsableSQLType 
				? (ParsableSQLType) type //improve parser search 
				: typeOf(type);
	}
	
	private static DBComparator containsArgPartten(StringComparator fn) {
		return (b, args)-> {
			args[1] = "%" + args[1] + "%";
			return fn.sql(b, args);
		};
	}
	
	public static ColumnDecorator countColumn() {
		return ofColumn(DBFunction.count().name(), t-> count());
	}

	public static ColumnDecorator ofColumn(String ref, ColumnBuilder cb) {
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
