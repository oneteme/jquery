package org.usf.jquery.web;

import static org.usf.jquery.web.ParsableSQLType.unparsableType;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.SQLType;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 * 
 * @see https://download.oracle.com/otn-pub/jcp/jdbc-4_2-mrel2-spec/jdbc4.2-fr-spec.pdf?AuthParam=1679342559_531aef55f72b5993f346322f9e9e7fe3
 * 
 */
@RequiredArgsConstructor
public enum ParsableJDBCType implements ParsableSQLType {
	
	AUTO_TYPE(JDBCType.AUTO_TYPE, ArgumentParser::tryParse), //replace by array type
	BOOLEAN(JDBCType.BOOLEAN, Boolean::parseBoolean),
	BIT(JDBCType.BIT, BOOLEAN::parse),
	TINYINT(JDBCType.TINYINT, Byte::parseByte),
	SMALLINT(JDBCType.SMALLINT, Short::parseShort),
	INTEGER(JDBCType.INTEGER, Integer::parseInt),
	BIGINT(JDBCType.BIGINT, Long::parseLong),
	REAL(JDBCType.REAL, Float::parseFloat),
	FLOAT(JDBCType.FLOAT, Double::parseDouble),
	DOUBLE(JDBCType.DOUBLE, FLOAT::parse),
	NUMERIC(JDBCType.NUMERIC, BigDecimal::new),
	DECIMAL(JDBCType.DECIMAL, BigDecimal::new),
	CHAR(JDBCType.CHAR, v-> v), //teradata !char
	VARCHAR(JDBCType.VARCHAR, CHAR::parse),
	NVARCHAR(JDBCType.NVARCHAR, CHAR::parse),
	LONGNVARCHAR(JDBCType.LONGNVARCHAR, CHAR::parse),
	DATE(JDBCType.DATE, v-> Date.valueOf(LocalDate.parse(v))),
	TIME(JDBCType.TIME, v-> Time.valueOf(LocalTime.parse(v))),
	TIMESTAMP(JDBCType.TIMESTAMP,v-> Timestamp.from(Instant.parse(v))),
	TIMESTAMP_WITH_TIMEZONE(JDBCType.TIMESTAMP_WITH_TIMEZONE, TIMESTAMP::parse);
	
	@Delegate
	private final SQLType type;
	@Delegate
	private final ArgumentParser parser;
	
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
}