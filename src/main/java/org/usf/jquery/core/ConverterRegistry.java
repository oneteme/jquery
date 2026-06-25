package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author u$f
 *
 */
public final class ConverterRegistry {

	private static final Map<Class<?>, TypeConverter<?>> DEF_CONVERTERS;

	private Map<Class<?>, TypeConverter<?>> converters;
	
	public <T> ConverterRegistry register(Class<T> clazz, TypeConverter<? super T> converter) {
		if(converters == null) {
			converters = new HashMap<>();
		}
		converters.put(clazz, converter);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> TypeConverter<T> getConverter(Class<T> object) {
		var v = nonNull(converters) ? converters.get(object.getClass()) : null;
		return (TypeConverter<T>) (isNull(v) ? DEF_CONVERTERS.get(object.getClass()) : v);
	}
	
	static {
		var map = new HashMap<Class<?>, TypeConverter<?>>();
		map.put(LocalDate.class, (TypeConverter<LocalDate>) ConverterRegistry::localDateConverter);
		map.put(LocalTime.class, (TypeConverter<LocalTime>) ConverterRegistry::localTimeConverter);
		map.put(Instant.class, (TypeConverter<Instant>) ConverterRegistry::instantConverter);
		DEF_CONVERTERS = Collections.unmodifiableMap(map);
	}
	
	public static Object localDateConverter(LocalDate date, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(date);
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.valueOf(date.atStartOfDay());
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
	
	public static Object localTimeConverter(LocalTime time, JDBCType type) {
		return switch (type) {
		case TIME-> Time.valueOf(time);
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
	
	public static Object instantConverter(Instant intant, JDBCType type) {
		return switch (type) {
		case DATE-> intant.atZone(ZoneId.of("UTC")).toLocalDate();
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(intant);
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
}
