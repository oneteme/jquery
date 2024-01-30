package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;

/**
 * 
 * @author u$f
 *
 */
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * <a href="https://docs.oracle.com/cd/E19830-01/819-4721/beajw/index.html">Supported Data Types</a>
 * 
 * @author u$f
 * 
 */
@Getter
@RequiredArgsConstructor
public enum JDBCType implements JavaType {

	BOOLEAN(Types.BOOLEAN, Boolean.class, JDBCType::isBoolean),
	BIT(Types.BIT, Boolean.class, JDBCType::isBoolean),
	TINYINT(Types.TINYINT, Byte.class, Number.class, Number.class::isInstance),
	SMALLINT(Types.SMALLINT, Short.class, Number.class, Number.class::isInstance),
	INTEGER(Types.INTEGER, Integer.class, Number.class, Number.class::isInstance),
	BIGINT(Types.BIGINT, Long.class, Number.class, Number.class::isInstance),
	REAL(Types.REAL, Float.class, Number.class, Number.class::isInstance),
	FLOAT(Types.FLOAT, Double.class, Number.class, Number.class::isInstance),
	DOUBLE(Types.DOUBLE, Double.class, Number.class, Number.class::isInstance),
	NUMERIC(Types.NUMERIC, BigDecimal.class, Number.class, Number.class::isInstance),
	DECIMAL(Types.DECIMAL, BigDecimal.class, Number.class, Number.class::isInstance),
	CHAR(Types.CHAR, Character.class, JDBCType::isChar), //teradata !char
	VARCHAR(Types.VARCHAR, String.class, JDBCType::isString),
	NVARCHAR(Types.NVARCHAR, String.class, JDBCType::isString),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class, JDBCType::isString),
	DATE(Types.DATE, Date.class, Date.class::isInstance),
	TIME(Types.TIME, Time.class, Time.class::isInstance),
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class, Timestamp.class::isInstance),
	TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp.class, Timestamp.class::isInstance),
	OTHER(Types.OTHER, Object.class, null) { //readonly
		@Override
		public boolean accept(Object o) {
			return false;
		}
	};

	private final int value;
	private final Class<?> type;
	private final Class<?> superType;
	private final Predicate<Object> matcher;
	
	private JDBCType(int value, Class<?> type, Predicate<Object> matcher) {
		this(value, type, type, matcher);
	}
	
	@Override
	public Class<?> typeClass() {
		return type;
	}
	
	@Override
	public boolean accept(Object o) {
		if(o instanceof Typed) {
			var t = ((Typed) o).getType();
			return t == this || isNull(t) || superType.isAssignableFrom(t.typeClass());
		}
		return isNull(o) || matcher.test(o);
	}
	
	private static boolean isBoolean(Object o) {
		return o.getClass() == Boolean.class 
				|| o.equals(0) || o.equals(1) 
				|| (o.getClass() == String.class && o.toString().matches("[yYnN]"));
	}
	
	private static boolean isChar(Object o) {
		return o.getClass() == Character.class 
				|| (o.getClass() == String.class && o.toString().length() == 1);
	}
	
	private static boolean isString(Object o) {
		return o.getClass() == Character.class 
				|| o.getClass() == String.class;
	}
	
	public static Optional<JDBCType> typeOf(Object o) {
		if(o instanceof Typed) {
			var t = ((Typed) o).getType();
			return t instanceof JDBCType ? Optional.of((JDBCType) t) : empty();
		}
		return Optional.of(o).flatMap(v-> findType(e-> e.typeClass().isInstance(o)));
	}
	
	public static Optional<JDBCType> fromDataType(int value) {
		return findType(t-> t.value == value);
	}
	
	public static Optional<JDBCType> findType(Predicate<JDBCType> predicate) {
		for(var t : values()) {
			if(predicate.test(t)) {
				return Optional.of(t);
			}
		}
		return empty();
	}
}