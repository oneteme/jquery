package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;

/**
 * 
 * <a href="https://docs.oracle.com/cd/E19830-01/819-4721/beajw/index.html">Supported Data Types</a>
 * 
 * @author u$f
 * 
 */
@RequiredArgsConstructor
public enum JDBCType implements JavaType {

	BOOLEAN(Types.BOOLEAN, Boolean.class, JDBCType::isBoolean),
	BIT(Types.BIT, Boolean.class, JDBCType::isBoolean),
	TINYINT(Types.TINYINT, Byte.class, Number.class),
	SMALLINT(Types.SMALLINT, Short.class, Number.class),
	INTEGER(Types.INTEGER, Integer.class, Number.class),
	BIGINT(Types.BIGINT, Long.class, Number.class),
	REAL(Types.REAL, Float.class, Number.class),
	FLOAT(Types.FLOAT, Double.class, Number.class),
	DOUBLE(Types.DOUBLE, Double.class, Number.class),
	NUMERIC(Types.NUMERIC, BigDecimal.class, Number.class),
	DECIMAL(Types.DECIMAL, BigDecimal.class, Number.class),
	CHAR(Types.CHAR, Character.class, JDBCType::isString),
	VARCHAR(Types.VARCHAR, String.class, JDBCType::isString),
	NVARCHAR(Types.NVARCHAR, String.class, JDBCType::isString),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class, JDBCType::isString),
	DATE(Types.DATE, Date.class),
	TIME(Types.TIME, Time.class),
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class),
	TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp.class),
	OTHER(Types.OTHER, Object.class) { //readonly
		@Override
		public boolean accept(Object o) {
			return false;
		}
	};

	private final int value;
	private final Class<?> type;
	private final Predicate<Class<?>> typeMatcher;
	private final Predicate<Object> valueMatcher;

	private <T> JDBCType(int value, Class<T> type) {
		this(value, type, type);
	}
	
	private <T> JDBCType(int value, Class<T> type, Class<? super T> supr) { //same parent
		this(value, type, supr::isAssignableFrom, supr::isInstance);
	}
	
	private JDBCType(int value, Class<?> type, Predicate<Object> valueMatcher) {
		this(value, type, type::isAssignableFrom, valueMatcher);
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
		return isNull(o) || valueMatcher.test(o);
	}
	
	
	private static boolean isBoolean(Object o) {
		return o.getClass() == Boolean.class 
				|| o.equals(0) || o.equals(1) 
				|| (o.getClass() == String.class && o.toString().matches("[yYnN]"));
	}
	
	private static boolean isString(Object o) {
		return o.getClass() == Character.class 
				|| o.getClass() == String.class;
	}
	
	public static Optional<JDBCType> typeOf(Object o) {
		return o instanceof Typed t 
				? ofNullable(t.getType())
				: ofNullable(o).flatMap(v-> findType(e-> e.type.isInstance(o)));
	}
	
	public static Optional<JDBCType> fromDataType(int value) {
		return findType(t-> t.value == value);
	}
	
	public static Optional<JDBCType> findType(Predicate<JDBCType> pre) {
		return stream(values()).filter(pre).findAny();
	}
}