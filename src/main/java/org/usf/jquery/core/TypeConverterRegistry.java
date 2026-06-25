package org.usf.jquery.core;

import static java.time.ZoneId.systemDefault;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author u$f
 *
 */
public final class TypeConverterRegistry {

	private static final Map<Class<?>, TypeConverter<?>> DEF_CONVERTERS;

	private Map<Class<?>, TypeConverter<?>> converters;
	
	public <T> TypeConverterRegistry register(Class<T> clazz, TypeConverter<? super T> converter) {
		if(converters == null) {
			converters = new HashMap<>();
		}
		converters.put(clazz, converter);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> TypeConverter<T> getConverter(Class<T> object) {
		var v = nonNull(converters) ? converters.get(object) : null;
		return (TypeConverter<T>) (isNull(v) ? DEF_CONVERTERS.get(object) : v);
	}
	
	static {
		var map = new HashMap<Class<?>, TypeConverter<?>>();
		map.put(LocalDate.class, (TypeConverter<LocalDate>) TypeConverterRegistry::localDateConverter);
		map.put(LocalTime.class, (TypeConverter<LocalTime>) TypeConverterRegistry::localTimeConverter);
		map.put(LocalTime.class, (TypeConverter<OffsetTime>) TypeConverterRegistry::offsetTimeConverter);
		map.put(LocalTime.class, (TypeConverter<OffsetDateTime>) TypeConverterRegistry::offsetDateTimeConverter);
		map.put(Instant.class, (TypeConverter<ZonedDateTime>) TypeConverterRegistry::zonedDateTimeConverter);
		map.put(Instant.class, (TypeConverter<Instant>) TypeConverterRegistry::instantConverter);
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
	
	public static Object offsetTimeConverter(OffsetTime time, JDBCType type) {
		return switch (type) {
		case TIME-> Time.valueOf(time.toLocalTime());
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
	
	public static Object offsetDateTimeConverter(OffsetDateTime odt, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(odt.toLocalDate());
		case TIME-> Time.valueOf(odt.toLocalTime());
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(odt.toInstant());
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
	
	public static Object zonedDateTimeConverter(ZonedDateTime zdt, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(zdt.toLocalDate());
		case TIME-> Time.valueOf(zdt.toLocalTime());
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(zdt.toInstant());
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
	
	public static Object instantConverter(Instant intant, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(intant.atZone(systemDefault()).toLocalDate());
		case TIME-> Time.valueOf(intant.atZone(systemDefault()).toLocalTime());
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(intant);
		default -> throw new IllegalArgumentException("unsupported conversion");
		};
	}
	
}
