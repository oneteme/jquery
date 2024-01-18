package org.usf.jquery.core;

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
	
	//do not change enum order
	BOOLEAN(Types.BOOLEAN, Boolean.class, JDBCType::isBoolean),
	BIT(Types.BIT, Boolean.class, JDBCType::isBoolean),
	TINYINT(Types.TINYINT, Byte.class, o-> isNumber(o, Byte.MIN_VALUE, Byte.MAX_VALUE, false)),
	SMALLINT(Types.SMALLINT, Short.class, o-> isNumber(o, Short.MIN_VALUE, Short.MAX_VALUE, false)),
	INTEGER(Types.INTEGER, Integer.class, o-> isNumber(o, Integer.MIN_VALUE, Integer.MAX_VALUE, false)),
	BIGINT(Types.BIGINT, Long.class, o-> isNumber(o, Long.MIN_VALUE, Long.MAX_VALUE, false)),
	REAL(Types.REAL, Float.class, o-> isNumber(o, Float.MIN_VALUE, Float.MAX_VALUE, true)),
	FLOAT(Types.FLOAT, Double.class, o-> isNumber(o, Double.MIN_VALUE, Double.MAX_VALUE, true)),
	DOUBLE(Types.DOUBLE, Double.class, o-> isNumber(o, Double.MIN_VALUE, Double.MAX_VALUE, true)),
	NUMERIC(Types.NUMERIC, BigDecimal.class, JDBCType::isNumber),
	DECIMAL(Types.DECIMAL, BigDecimal.class, JDBCType::isNumber),
	CHAR(Types.CHAR, Character.class, JDBCType::isChar), //teradata !char
	VARCHAR(Types.VARCHAR, String.class, JDBCType::isString),
	NVARCHAR(Types.NVARCHAR, String.class, JDBCType::isString),
	LONGNVARCHAR(Types.LONGNVARCHAR, String.class, JDBCType::isString),
	DATE(Types.DATE, Date.class, Date.class::isInstance),
	TIME(Types.TIME, Time.class, Time.class::isInstance),
	TIMESTAMP(Types.TIMESTAMP, Timestamp.class, Timestamp.class::isInstance),
	TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp.class, Timestamp.class::isInstance),
	OTHER(Types.OTHER, Object.class, o-> false);

	private final int value;
	private final Class<?> type;
	private final Predicate<Object> matcher;
	
	@Override
	public Class<?> type() {
		return type;
	}
	
	@Override
	public boolean accept(Object o) {
		if(o instanceof Typed) {
			var t = ((Typed) o).javaType();
			return t == null 
					|| this == t
					|| type() == t.type()
					|| (subType(this, Number.class) && subType(t, Number.class)); //other types compatibility
		}
		return acceptValue(o);
	}
	
	static boolean subType(JavaType type, Class<?> c) {
		return c.isAssignableFrom(type.type());
	}
	
	private boolean acceptValue(Object o) {
		return o == null || matcher.test(o);
	}
	
	private static boolean isNumber(Object o, double min, double max, boolean decimal) {
		if(isNumber(o)) {
			var n = (Number) o;
			var v = n.doubleValue();
			return (v >= min && v <= max) && (decimal || v == n.longValue());
		}
		return false;
	}

	private static boolean isNumber(Object o) {
		return o instanceof Number;
	}
	
	private static boolean isBoolean(Object o) {
		return o.getClass() == Boolean.class 
				|| isNumber(o, 0, 1, false)
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
			var t = ((Typed) o).javaType();
			return t instanceof JDBCType ? Optional.of((JDBCType) t) : empty();
		}
		return Optional.of(o).flatMap(v-> findType(e-> e.type().isInstance(o)));
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