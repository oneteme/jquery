package org.usf.jquery.web.proxy;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.proxy.Resource.findMethod;
import static org.usf.jquery.web.proxy.Resource.invokeResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.web.EntryParseException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class QueryContext {

	private static final JDBCType[] STD_TYPES = { 
			BIGINT, DOUBLE, DATE, TIMESTAMP, 
			TIME, TIMESTAMP_WITH_TIMEZONE, VARCHAR };

	//TODO allowLiteralJoin, allowLiteralQuery, ..
	private final DBSchema schema;
	private final DBView defaultView;
	private final Map<String, DBView> cache = new HashMap<>();
	private final TypeParserRegistry registry;
	
	public Optional<DBView> lookupView(String name, EntryChain... args) { 
		var view = cache.get(name);
		if(isNull(view)) {
			var mth = findMethod(schema, name);
			if(nonNull(mth) && DBView.class.isAssignableFrom(mth.getReturnType())) {
				view = DBView.class.cast(invokeResource(mth, schema, args, this));
			}
		}
		return ofNullable(view);
	}
	
	public <T> Optional<T> lookupViewResource(DBView view, String name, Class<T> type, EntryChain... args) { 
		var mth = findMethod(view, name);
		return nonNull(mth) && type.isAssignableFrom(mth.getReturnType()) 
				? Optional.of(type.cast(invokeResource(mth, schema, args, this)))
				: empty();
	}
	
	void addView(String name, DBView view) {
		cache.compute(name, (k,v)->{
			if(nonNull(v)) {
				throw new IllegalArgumentException("a view with name '" + name + "' already exists in context");
			}
			return view;
		});
	}
	
	public Object eval(EntryChain entry, JavaType... types) {
		if(entry.isVariable() && (isEmpty(types) || Stream.of(types).allMatch(JDBCType.class::isInstance))) {
			try {
				return eval(entry, DBColumn.class);
			}
			catch (Exception e) {
				try {
					return eval(entry, QueryView.class);
				}
				catch (Exception ex) {
					//do nothing, try other types
				}
			}
		}
		if(isEmpty(types)) {
			types = STD_TYPES;
		}
		for(var t : types) {
			try {
				return eval(entry, t.getCorrespondingClass());
			}
			catch (EntryParseException e) {
				throw e; //cannot parse entry for this type, no need to try other types
			}
			catch (Exception e) {
				//do nothing, try other types
			}
		}
		throw new NoSuchElementException("no parser for type " + Arrays.toString(types));
	}

	public Object eval(EntryChain entry, Class<?> type) {
		if(entry.isVariable()) {
			var prs = registry.getVariableParser(type);
			if(nonNull(prs)) {
				try {
					return prs.parse(entry, this);
				}
				catch (Exception e) {
					throw cannotParseEntryException(type, entry.getValue());
				}
			}
		}
		if(!entry.hasArgs() && !entry.hasNext() && !entry.hasTag()) {
			return evalValue(entry.getValue(), type);
		}
		throw new IllegalArgumentException("unkwown");
	}
	
	public Object evalValue(String value, Class<?> type) {
		var prs = registry.getValueParser(type);
		if(nonNull(prs)) {
			try {
				return prs.parse(value);
			}
			catch (Exception e) {
				throw cannotParseEntryException(type, value);
			}
		}
		throw new NoSuchElementException("no parser for type " + type.getSimpleName());
	}
	
	static EntryParseException cannotParseEntryException(Class<?> type, String v) {
		return new EntryParseException("cannot parse '" + type.getSimpleName() + "' value " + v);
	}
	
	public QueryContext subContext(DBView view) {
		return new QueryContext(schema, view, registry);
	}
}
