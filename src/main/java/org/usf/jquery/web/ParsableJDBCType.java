package org.usf.jquery.web;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.ParsableSQLType.unparsableType;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.SQLType;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 * 
 */
@RequiredArgsConstructor
public enum ParsableJDBCType implements ParsableSQLType {
	
	AUTO_TYPE(JDBCType.AUTO_TYPE, ParsableJDBCType::tryParse), //replace by array type
	BOOLEAN(JDBCType.BOOLEAN, Boolean::parseBoolean),
	BIT(JDBCType.BIT, BOOLEAN::parse),
	TINYINT(JDBCType.TINYINT, Byte::parseByte),
	SMALLINT(JDBCType.SMALLINT, Short::parseShort),
	INTEGER(JDBCType.INTEGER, Integer::parseInt),
	BIGINT(JDBCType.BIGINT, Long::parseLong),
	REAL(JDBCType.REAL, Float::parseFloat),
	FLOAT(JDBCType.FLOAT, Double::parseDouble),
	DOUBLE(JDBCType.DOUBLE, Double::parseDouble),
	NUMERIC(JDBCType.NUMERIC, BigDecimal::new),
	DECIMAL(JDBCType.DECIMAL, BigDecimal::new),
	CHAR(JDBCType.CHAR, v-> v), //teradata !char
	VARCHAR(JDBCType.VARCHAR, v-> v),
	NVARCHAR(JDBCType.NVARCHAR, v-> v),
	LONGNVARCHAR(JDBCType.LONGNVARCHAR, v-> v),
	DATE(JDBCType.DATE, v-> Date.valueOf(LocalDate.parse(v))),
	TIME(JDBCType.TIME, v-> Time.valueOf(LocalTime.parse(v))),
	TIMESTAMP(JDBCType.TIMESTAMP,v-> Timestamp.from(Instant.parse(v))),
	TIMESTAMP_WITH_TIMEZONE(JDBCType.TIMESTAMP_WITH_TIMEZONE, TIMESTAMP::parse);

	private static final List<ArgumentParser> COMMON_PARSERS = asList(
//			Boolean, not throw exception
			BIGINT, DOUBLE, // byte, short, integer, float
			DATE, TIME, TIMESTAMP); //else string
	
	@Delegate
	private final SQLType type;
	@Delegate
	private final ArgumentParser parser; //isAutoType delegated
	
	public static ParsableSQLType typeOf(int type) {
		for(var t : values()) {
			if(t.type.getValue() == type) {
				return t;
			}
		}
		return unparsableType(type);
	}

	public static ParsableSQLType typeOf(SQLType type) {
		for(var t : values()) {
			if(t.type == type) {
				return t;
			}
		}
		return unparsableType(type);
	}
	
	public static Object tryParse(String value) {
		if(nonNull(value)) {
			for(var p : COMMON_PARSERS) {
				try {
					return p.nativeParse(value);
				}
				catch (Exception e) {/* do not handle exception */}
			}
		}
		return value;  //default type String
	}
}