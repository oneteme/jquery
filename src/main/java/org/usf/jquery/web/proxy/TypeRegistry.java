package org.usf.jquery.web.proxy;

import static java.lang.Character.MIN_VALUE;
import static java.text.DateFormat.getDateInstance;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.SingleQueryColumn;

/**
 * 
 * @author u$f
 *
 */
public final class TypeRegistry {
	
	private static final Map<Class<?>, ValueParser<?>> VAL_PARSERS;
	private static final Map<Class<?>, EntryEvaluator<?>> VAR_PARSERS;
	
	private final Map<Class<?>, ValueParser<?>> valParsers;
	private final Map<Class<?>, EntryEvaluator<?>> varParsers;
	
	public TypeRegistry() {
		this.valParsers = new ConcurrentHashMap<>(VAL_PARSERS);
		this.varParsers = new ConcurrentHashMap<>(VAR_PARSERS);
	}
	
	public <T> void register(Class<T> clazz, ValueParser<T> parser) {
		valParsers.put(clazz, parser);
	}
	
	public <T> void register(Class<T> clazz, EntryEvaluator<T> parser) {
		varParsers.put(clazz, parser);
	}
	
	@SuppressWarnings("unchecked")
	public <T> ValueParser<T> getValueParser(Class<T> clazz){
		var p = valParsers.get(clazz);
		if(isNull(p) && Enum.class.isAssignableFrom(clazz)) {
			p = v-> Enum.valueOf(clazz.asSubclass(Enum.class), v);
		}
		return (ValueParser<T>) p;
	}
	
	@SuppressWarnings("unchecked")
	public <T> EntryEvaluator<T> getVariableParser(Class<T> clazz){
		return (EntryEvaluator<T>) varParsers.get(clazz);
	}
	
	static {
		var smpl = new HashMap<Class<?>, ValueParser<?>>();
		smpl.put(Boolean.class, TypeRegistry::parseBoolean); //db boolean compatibility
		smpl.put(Byte.class, Byte::parseByte);
		smpl.put(Short.class, Short::parseShort);
		smpl.put(Integer.class, Integer::parseInt);
		smpl.put(Long.class, Long::parseLong);
		smpl.put(Float.class, Float::parseFloat);
		smpl.put(Double.class, Double::parseDouble);
		smpl.put(Character.class, TypeRegistry::parseChar);
		smpl.put(BigInteger.class, BigInteger::new);
		smpl.put(BigDecimal.class, BigDecimal::new);
		smpl.put(String.class, v-> v);
		smpl.put(LocalDate.class, LocalDate::parse);
		smpl.put(LocalTime.class, LocalTime::parse);
		smpl.put(LocalDateTime.class, LocalDateTime::parse);
		smpl.put(OffsetDateTime.class, OffsetDateTime::parse);
		smpl.put(ZonedDateTime.class, ZonedDateTime::parse);
		smpl.put(Instant.class, Instant::parse);
		smpl.put(YearMonth.class, YearMonth::parse);
		smpl.put(MonthDay.class, MonthDay::parse);
		smpl.put(java.util.Date.class, getDateInstance()::parse);
		smpl.put(Date.class, Date::valueOf);
		smpl.put(Timestamp.class, Timestamp::valueOf);
		smpl.put(UUID.class, UUID::fromString);
		//Object ?
		VAL_PARSERS = unmodifiableMap(smpl);
		var expr = new HashMap<Class<?>, EntryEvaluator<?>>();
		expr.put(DBColumn.class, EntryEvaluators::evaluateColumn);
		expr.put(NamedColumn.class, EntryEvaluators::evaluateNamedColumn);
		expr.put(DBFilter.class, EntryEvaluators::evaluateFilter);
		expr.put(DBOrder.class, EntryEvaluators::evaluateOrder);
		expr.put(JoinsClause.class, EntryEvaluators::evaluateJoin);
		expr.put(Partition.class, EntryEvaluators::evaluatePartition);
		expr.put(DBView.class, EntryEvaluators::evaluateView);
		expr.put(SingleQueryColumn.class, EntryEvaluators::evaluateQueryColumn);
		VAR_PARSERS = unmodifiableMap(expr);
	}
	
	static Boolean parseBoolean(String v) throws ParseException {
		if(nonNull(v)) {
			if(v.length() == 1) {
				var c = v.charAt(0);
				if(c=='1' || c=='t') {
					return true;
				}
				if(c=='0' || c=='f') {
					return false;
				}
			}
			else if(v.matches("true|false")) {
				return Boolean.parseBoolean(v); //not thrown exception
			}
			throw new ParseException("cannot parse boolean " + v, -1);
		}
		return null;
	}
	
	static Character parseChar(String v) throws ParseException {
		if(nonNull(v)) {
			if(v.length() > 1) {
				throw new ParseException("cannot parse char " + v, 1);
			}
			return v.isEmpty() ? MIN_VALUE : v.charAt(0);
		}
		return null;
	}

	@FunctionalInterface
	public static interface ValueParser<T> {
		
		T parse(String s) throws Exception;	//parse Exception
	}
	
	@FunctionalInterface
	public static interface EntryEvaluator<T> {
		
		T evaluate(Entry entry, QueryContext ctx) throws Exception;		
	}
}
