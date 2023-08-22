package org.usf.jquery.core;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 
 * @author u$f
 * 
 */
@Getter
@ToString
@RequiredArgsConstructor
public enum JDBCType implements SQLType {
	
	AUTO_TYPE(Types.NULL, Object.class), //replace by array type
	BOOLEAN(Types.BOOLEAN, Boolean.class),
	BIT(Types.BIT, Boolean.class),
	TINYINT(Types.TINYINT, Byte.class),
	SMALLINT(Types.SMALLINT, Short.class),
	INTEGER(Types.INTEGER, Integer.class),
	BIGINT(Types.BIGINT, Long.class),
	REAL(Types.REAL, Float.class),
	FLOAT(Types.FLOAT, Double.class),
	DOUBLE(Types.DOUBLE, Double.class),
	NUMERIC(Types.NUMERIC, BigDecimal.class),
	DECIMAL(Types.DECIMAL, BigDecimal.class),
	CHAR(Types.CHAR, String.class), //teradata !char
	VARCHAR(Types.VARCHAR, String.class),
	NVARCHAR(Types.NVARCHAR, String.class),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class),
	DATE(Types.DATE, Date.class),
	TIME(Types.TIME, Time.class),
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class),
	TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp.class);
	
	private final int value;
	private final Class<?> javaType;
	
	@Override
	public boolean isAutoType() {
		return this == AUTO_TYPE;
	}
}