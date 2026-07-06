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
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author u$f
 *
 */
public class TypeConverterRegistry {

	private static final Map<Class<?>, TypeConverter<?>> DEF_CONVERTERS = Map.of(
			LocalDate.class, (TypeConverter<LocalDate>) TypeConverterRegistry::localDateConverter,
			LocalTime.class, (TypeConverter<LocalTime>) TypeConverterRegistry::localTimeConverter,
			OffsetTime.class, (TypeConverter<OffsetTime>) TypeConverterRegistry::offsetTimeConverter,
			OffsetDateTime.class, (TypeConverter<OffsetDateTime>) TypeConverterRegistry::offsetDateTimeConverter,
			ZonedDateTime.class, (TypeConverter<ZonedDateTime>) TypeConverterRegistry::zonedDateTimeConverter,
			Instant.class, (TypeConverter<Instant>) TypeConverterRegistry::instantConverter);

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
		default -> throw unsupportedConversionException(time,type);
		};
	}
	
	public static Object offsetTimeConverter(OffsetTime offTime, JDBCType type) {
		return switch (type) {
		case TIME-> Time.valueOf(offTime.toLocalTime());
		default -> throw unsupportedConversionException(offTime, type);
		};
	}
	
	public static Object offsetDateTimeConverter(OffsetDateTime odt, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(odt.toLocalDate());
		case TIME-> Time.valueOf(odt.toLocalTime());
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(odt.toInstant());
		default -> throw unsupportedConversionException(odt, type);
		};
	}
	
	public static Object zonedDateTimeConverter(ZonedDateTime zdt, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(zdt.toLocalDate());
		case TIME-> Time.valueOf(zdt.toLocalTime());
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(zdt.toInstant());
		default -> throw unsupportedConversionException(zdt, type);
		};
	}
	
	public static Object instantConverter(Instant intant, JDBCType type) {
		return switch (type) {
		case DATE-> Date.valueOf(intant.atZone(systemDefault()).toLocalDate());
		case TIME-> Time.valueOf(intant.atZone(systemDefault()).toLocalTime());
		case TIMESTAMP, TIMESTAMP_WITH_TIMEZONE-> Timestamp.from(intant);
		default -> throw unsupportedConversionException(intant, type);
		};
	}
	
	private static UnsupportedOperationException unsupportedConversionException(Object o, JDBCType type) {
		return new UnsupportedOperationException(("Unsupported conversion for %s to %s").formatted(o, type));
	}
}
