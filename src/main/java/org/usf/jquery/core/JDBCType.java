package org.usf.jquery.core;

import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

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
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.Set;

/**
 * 
 * @author u$f
 * 
 */
public enum JDBCType implements JavaType, TypeResolver {
	
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
	
	private static final JDBCType[] NUMBERS = {TINYINT, SMALLINT, INTEGER, BIGINT, REAL, FLOAT, DOUBLE, NUMERIC, DECIMAL};
	private static final JDBCType[] TEMPORALS = {DATE, TIME, TIMESTAMP, TIMESTAMP_WITH_TIMEZONE};

	private final int value;
	private final Class<?> type;
	private final boolean isNumber;
	private final Set<Class<?>> accepts;

	private JDBCType(int value, Class<?> type, Class<?>... accepts) {
		this.value = value;
		this.type = type;
		this.isNumber = Number.class.isAssignableFrom(type);
		this.accepts = nonNull(accepts) ? Set.of(accepts) : emptySet();
	}

	public int getValue() {
		return value;
	}

	@Override
	public Class<?> getCorrespondingClass() {
		return type;
	}

	@Override
	public boolean accept(Object o) {
		if(o instanceof Typed v) {
			var t = v.getType();
			return t == this || isNull(t) || accept(t.type);
		}
		return isNull(o) || accept(o.getClass());
	}

	public boolean accept(Class<?> c) {
		return type == c || (isNumber ? Number.class.isAssignableFrom(c) : accepts.contains(c));
	}

	public static Optional<JDBCType> typeOf(Object o) {
		if(nonNull(o)) {
			if(o instanceof Typed t) {
				return ofNullable(t.getType());
			}
			if(o instanceof Number) {
				return typeOf(o, NUMBERS);
			}
			if(o.getClass() == String.class) {
				return Optional.of(VARCHAR); //JSON, CLOB, .. ?
			}
			if(o instanceof java.util.Date || o instanceof Temporal) {
				return typeOf(o, TEMPORALS);
			}
			if(o.getClass() == Boolean.class) {
				return Optional.of(BIT);
			}
			if(o.getClass() == java.util.UUID.class) {
				return Optional.of(UUID);
			}
		}
		return empty();
	}
	
	
	static Optional<JDBCType> typeOf(Object o, JDBCType... types) {
		var c = o.getClass();
		for(var t : types) {
			if(c == t.type || t.accepts.contains(c)) {
				return Optional.of(t);
			}
		}
		return empty();
	}

	public static Optional<JDBCType> fromDataType(int value) {
		var arr = values();
		for(var v : arr) {
			if(v.value == value) {
				return Optional.of(v);
			}
		}
		return empty();
	}

	@Override
	public JDBCType apply(Object[] t) {
		return this;
	}
}