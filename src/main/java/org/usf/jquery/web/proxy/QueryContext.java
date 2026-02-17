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
import static org.usf.jquery.web.proxy.Resource.invokeResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.DBColumn;
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
	private final Resource schema;
	private final Resource defaultView;
	private final Map<String, Resource> cache;
	private final TypeParserRegistry registry;

	public QueryContext(Resource schema, Resource defaultView, TypeParserRegistry registry) {
		this(schema, defaultView, new HashMap<>(), registry);
	}
	
	public Optional<Resource> lookupView(boolean allowParameterize, String name, EntryChain... args) { 
		var view = cache.get(name);
		if(isNull(view)) {
			var mth = schema.lookupMethod(name, Resource.class);
			if(nonNull(mth)) {
				if(!allowParameterize && mth.getParameterCount() > 0) {
					throw new IllegalArgumentException("view resource '" + mth.getName() + "' expects parameters, but parameterization is not allowed in this context");
				} //else explicit view add
				view = Resource.class.cast(invokeResource(mth, schema, args, this));
				declareView(name, view);
			}
		}
		return ofNullable(view);
	}

	public <T> Optional<T> lookupSchemaResource(String name, Class<T> type, EntryChain... args) { 
		return invoke(schema, name, type, args);
	}
	
	public <T> Optional<T> lookupViewResource(Resource view, String name, Class<T> type, EntryChain... args) { 
		return invoke(view, name, type, args);
	}
	
	 <T> Optional<T> invoke(Object obj, String name, Class<T> type, EntryChain... args){
		var mth = schema.lookupMethod(name, type);
		return nonNull(mth)
				? Optional.of(type.cast(invokeResource(mth, obj, args, this)))
				: empty();
	}
	
	void declareView(String name, Resource view) {
		cache.compute(name, (k,v)->{
			if(isNull(v) || v == view) {
				return view;
			}
			throw new IllegalArgumentException("a view with name '" + name + "' already exists in context");
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

	public QueryContext subContext(Resource view) {
		return new QueryContext(schema, view, null, registry);
	}
	
	public QueryContext map(Resource view) { //inherit cache
		return new QueryContext(schema, view, cache, registry);
	}
		
	static EntryParseException cannotParseEntryException(Class<?> type, String v) {
		return new EntryParseException("cannot parse '" + type.getSimpleName() + "' value " + v);
	}
}
