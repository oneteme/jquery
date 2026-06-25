package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.Utils.isEmpty;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@RequiredArgsConstructor
public enum JDBCType implements JavaType, TypeResolver {

	BOOLEAN(Types.BOOLEAN, Boolean.class),
	BIT(Types.BIT, Boolean.class),
	
	TINYINT(Types.TINYINT, Byte.class, Number.class::isAssignableFrom),
	SMALLINT(Types.SMALLINT, Short.class, Number.class::isAssignableFrom),
	INTEGER(Types.INTEGER, Integer.class, Number.class::isAssignableFrom),
	BIGINT(Types.BIGINT, Long.class, Number.class::isAssignableFrom),
	REAL(Types.REAL, Float.class, Number.class::isAssignableFrom),
	FLOAT(Types.FLOAT, Double.class, Number.class::isAssignableFrom),
	DOUBLE(Types.DOUBLE, Double.class, Number.class::isAssignableFrom),
	NUMERIC(Types.NUMERIC, BigDecimal.class, Number.class::isAssignableFrom),
	DECIMAL(Types.DECIMAL, BigDecimal.class, Number.class::isAssignableFrom),
	
	CHAR(Types.CHAR, String.class),
	VARCHAR(Types.VARCHAR, String.class),
	NVARCHAR(Types.NVARCHAR, String.class),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class),
	
	DATE(Types.DATE, Date.class, java.util.Date.class, LocalDate.class),
	TIME(Types.TIME, Time.class, LocalTime.class, OffsetTime.class),
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class, 
			LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, Instant.class),
	TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp.class, 
			LocalDateTime.class, OffsetDateTime.class, ZonedDateTime.class, Instant.class),
	//check new types
	UUID(Types.OTHER, java.util.UUID.class),
	JSON(Types.OTHER, String.class),
	CLOB(Types.CLOB, String.class),
	BLOB(Types.BLOB, byte[].class),
	BINARY(Types.BINARY, byte[].class),
	//BLOB, CLOB, BINARY, JSON, ...
	OTHER(Types.OTHER, Object.class) { //readonly
		@Override
		public boolean accept(Object o) {
			return false;
		}
	};

	private final int value;
	private final Class<?> type;
	private final Predicate<Class<?>> typeMatcher;
	
	private <T> JDBCType(int value, Class<T> type, Class<?>... others) {
		this(value, type, isEmpty(others) ? c-> c == type : c-> {
			if(c == type) {
				return true;
			}
			for(var o : others) {
				if(c == o) {
					return true;
				}
			}
			return false;
		});
	}
	
	public Class<?> getCorrespondingClass() {
		return type;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public boolean accept(Object o) {
		if(o instanceof Typed v) {
			var t = v.getType();
			return t == this || isNull(t) || typeMatcher.test(t.type);
		}
		return isNull(o) || typeMatcher.test(o.getClass());
	}
	
	public static Optional<JDBCType> typeOf(Object o) {
		if(o instanceof Typed t) {
			return ofNullable(t.getType());
		}
		if(o instanceof String) {
			return Optional.of(VARCHAR);
		}
		if(o instanceof Number) {
			return typeOfNumber(o);
		}
		if(o instanceof Timestamp) {
			return Optional.of(TIMESTAMP);
		}
		if(o instanceof Time) {
			return Optional.of(TIME);
		}
		if(o instanceof Date) {
			return Optional.of(DATE);
		}
		if(o instanceof Boolean) {
			return Optional.of(BOOLEAN);
		}
		return empty();
	}
	
	public static Optional<JDBCType> typeOfNumber(Object o) {
		var c = o.getClass();
		if(c == Integer.class) {
			return Optional.of(INTEGER);
		}
		if(c == Long.class) {
			return Optional.of(BIGINT);
		}
		if(c == Double.class) {
			return Optional.of(DOUBLE);
		}
		if(c == BigDecimal.class) {
			return Optional.of(DECIMAL);
		}
		if(c == Float.class) {
			return Optional.of(REAL);
		}
		if(c == Short.class) {
			return Optional.of(SMALLINT);
		}
		if(c == Byte.class) {
			return Optional.of(TINYINT);
		}
		return empty();
	}
	
	public static Optional<JDBCType> fromDataType(int value) {
		return stream(values()).filter(t-> t.value == value).findAny();
	}

	@Override
	public JDBCType apply(Object[] t) {
		return this;
	}
}