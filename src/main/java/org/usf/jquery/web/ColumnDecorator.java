package org.usf.jquery.web;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.NVARCHAR;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
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
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.core.Validation.requireLegalVariable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

/**
 * 
 * @author u$f
 *
 */
public interface ColumnDecorator extends ColumnBuilder {
	
	String identity();  //URL
	
	String reference(); //JSON
	
	@Override
	default TaggableColumn column(TableDecorator table) {
		if(isPhysical()) {
			var sql = requireLegalVariable(table.columnName(this));
			return new TableColumn(sql, reference(), table.reference());
		}
		return columnBuilder().column(table).as(reference());
	}
	
	default ComparisonExpression expression(TableDecorator table, String comparator, String... values) {
		var cmp = requireNonNull(comparator(comparator, values.length));
    	var psr = requireNonNull(parser(resolveType(table)));
    	return cmp.expression(values.length == 1 
    			? psr.parseArg(values[0]) 
    			: psr.parseArgs(values));
	}
	
	default ColumnBuilder columnBuilder() {
		return this;
	}
	
	private int resolveType(TableDecorator td) {
		var type = dataType(); //overridden
		if(type == AUTO_TYPE && isPhysical()) {//logical column not declared in table
			type = td.columnType(this);
		}
		return type;
	}
	
	default int dataType() {
		return AUTO_TYPE;
	}

	default int dataSize() {
		return UNLIMITED;
	}

	default boolean canSelect() {
		return true;
	}

	default boolean canFilter() {
		return true;
	}
	
	default boolean isPhysical() {
		return columnBuilder() == this;
	}

	/**
	 * see: https://download.oracle.com/otn-pub/jcp/jdbc-4_2-mrel2-spec/jdbc4.2-fr-spec.pdf?AuthParam=1679342559_531aef55f72b5993f346322f9e9e7fe3
	 * 
	 * override parser | format | local
	 */
	default ArgumentParser parser(int type){
		switch(type) {
		case AUTO_TYPE		: return ArgumentParser::tryParse; //performance : must set db type
		case BOOLEAN		:
		case BIT		  	: return Boolean::parseBoolean;
		case TINYINT  		: return Byte::parseByte;
		case SMALLINT 		: return Short::parseShort;
		case INTEGER  		: return Integer::parseInt;
		case BIGINT   		: return Long::parseLong;
		case REAL 	  		: return Float::parseFloat;
		case FLOAT	  		: 
		case DOUBLE  		: return Double::parseDouble;
		case NUMERIC 		: 
		case DECIMAL  		: return BigDecimal::new;
		case CHAR  	  		: 
		case VARCHAR  		:
		case NVARCHAR  		:
		case LONGNVARCHAR	: return v-> v;
		case DATE     		: return v-> Date.valueOf(LocalDate.parse(v));
		case TIME     		: return v-> Time.valueOf(LocalTime.parse(v));
		case TIMESTAMP		: return v-> Timestamp.from(Instant.parse(v));
		default       		: throw new UnsupportedOperationException(identity() + " unsupported dbType " + type);
		}
	}

	default DBComparator comparator(String comparator, int nArg) {
		if(isNull(comparator)) {
			return nArg == 1 ? equal() : in();
		}
		switch(comparator) { 
		case "gt"	: return greaterThan();
		case "ge"  	: return greaterOrEqual();
		case "lt"  	: return lessThan();
		case "le"  	: return lessOrEqual();
		case "not" 	: return nArg == 1 ? notEqual() : notIn();
		case "like"	: return (b, args)-> {
			args[1] = "%" + args[1] + "%"; //not works with columns
			return like().sql(b, args);
		};
		case "ilike": return (b, args)-> {
			args[1] = "%" + args[1] + "%"; //not works with columns
			return iLike().sql(b, args);
		};
		//isnull
		default: throw new IllegalArgumentException("unsupported comparator : " + comparator);
		}
	}
}
