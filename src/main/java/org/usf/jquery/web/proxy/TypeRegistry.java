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

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.SingleQueryColumn;

/**
 * 
 * @author u$f
 *
 */
public final class TypeRegistry {
	
	private static final Map<Class<?>, ValueParser<?>> DEF_PARSERS;
	private static final Map<Class<?>, EntryEvaluator<?>> DEF_EVALUATORS;
	
	private final Map<Class<?>, ValueParser<?>> parsers;
	private final Map<Class<?>, EntryEvaluator<?>> evaluators;
	
	public TypeRegistry() {
		this.parsers = new HashMap<>();
		this.evaluators = new HashMap<>();
	}
	
	public <T> TypeRegistry register(Class<T> clazz, ValueParser<T> parser) {
		parsers.put(clazz, parser);
		return this;
	}
	
	public <T> TypeRegistry register(Class<T> clazz, EntryEvaluator<T> evaluator) {
		evaluators.put(clazz, evaluator);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> ValueParser<T> getParser(Class<T> clazz){
		var p = parsers.get(clazz);
		if(isNull(p) && Enum.class.isAssignableFrom(clazz)) { //strict | toUPPER
			p = v-> Enum.valueOf(clazz.asSubclass(Enum.class), v);
		}
		return (ValueParser<T>) (nonNull(p) ? p : DEF_PARSERS.get(clazz));
	}
	
	@SuppressWarnings("unchecked")
	public <T> EntryEvaluator<T> getEvaluator(Class<T> clazz){
		var p = evaluators.get(clazz);
		return (EntryEvaluator<T>) (nonNull(p) ? p : DEF_EVALUATORS.get(clazz));
	}
	
	static {
		var prs = new HashMap<Class<?>, ValueParser<?>>();
		prs.put(Boolean.class, TypeRegistry::parseBoolean); //db boolean compatibility
		prs.put(Byte.class, Byte::parseByte);
		prs.put(Short.class, Short::parseShort);
		prs.put(Integer.class, Integer::parseInt);
		prs.put(Long.class, Long::parseLong);
		prs.put(Float.class, Float::parseFloat);
		prs.put(Double.class, Double::parseDouble);
		prs.put(Character.class, TypeRegistry::parseChar);
		prs.put(BigInteger.class, BigInteger::new);
		prs.put(BigDecimal.class, BigDecimal::new);
		prs.put(String.class, v-> v);
		prs.put(LocalDate.class, LocalDate::parse);
		prs.put(LocalTime.class, LocalTime::parse);
		prs.put(LocalDateTime.class, LocalDateTime::parse);
		prs.put(OffsetDateTime.class, OffsetDateTime::parse);
		prs.put(ZonedDateTime.class, ZonedDateTime::parse);
		prs.put(Instant.class, Instant::parse);
		prs.put(YearMonth.class, YearMonth::parse);
		prs.put(MonthDay.class, MonthDay::parse);
		prs.put(java.util.Date.class, getDateInstance()::parse);
		prs.put(Date.class, Date::valueOf);
		prs.put(Timestamp.class, Timestamp::valueOf);
		prs.put(UUID.class, UUID::fromString);
		//Object ?
		DEF_PARSERS = unmodifiableMap(prs);
		var evl = new HashMap<Class<?>, EntryEvaluator<?>>();
		evl.put(Column.class, EntryEvaluators::evaluateColumn);
		evl.put(NamedColumn.class, EntryEvaluators::evaluateNamedColumn);
		evl.put(Criteria.class, EntryEvaluators::evaluateFilter);
		evl.put(Order.class, EntryEvaluators::evaluateOrder);
		evl.put(JoinsClause.class, EntryEvaluators::evaluateJoin);
		evl.put(Partition.class, EntryEvaluators::evaluatePartition);
		evl.put(DBView.class, EntryEvaluators::evaluateView);
		evl.put(SingleQueryColumn.class, EntryEvaluators::evaluateQueryColumn);
		DEF_EVALUATORS = unmodifiableMap(evl);
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
		
		T evaluate(Entry entry, RequestContext ctx);		
	}
}
