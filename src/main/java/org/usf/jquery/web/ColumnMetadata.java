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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor 
public final class ColumnMetadata implements ArgumentParser {

	private final int type;
	private final int length;
	private final boolean canSelect;
	private final boolean canFilter;
	private ArgumentParser parser;

	@Override
	public Object parse(String v) {
		if(parser == null) {
			parser = parser(type, name); // lazy load
		}
		return parser.parse(v); //can check string.size < length
	}

	/**
	 * see: https://download.oracle.com/otn-pub/jcp/jdbc-4_2-mrel2-spec/jdbc4.2-fr-spec.pdf?AuthParam=1679342559_531aef55f72b5993f346322f9e9e7fe3
	 * @return
	 */
	static ArgumentParser parser(int type, String name){
		switch(type) {
		case BOOLEAN:
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
		case DATE     		: return v-> Date.valueOf(LocalDate.parse(v)); //TD check
		case TIME     		: return v-> Time.valueOf(LocalTime.parse(v)); //TD check
		case TIMESTAMP		: return v-> Timestamp.from(Instant.parse(v)); //TD check
		default       		: throw new UnsupportedOperationException(name + " unsupported dbType " + type);
		}
	}
	
	@Override
	public String toString() {
		return "(name="+ name + ", type=" + type + ", length=" + length + ")";
	}
}