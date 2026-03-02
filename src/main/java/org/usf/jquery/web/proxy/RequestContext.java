package org.usf.jquery.web.proxy;

import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.NoSuchResourceException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class RequestContext {

	private static final JDBCType[] STD_TYPES = { 
			BIGINT, DOUBLE, DATE, TIMESTAMP, 
			TIME, TIMESTAMP_WITH_TIMEZONE, VARCHAR };

	private final Resource schema;
	private final DatasetResource defaultView;
	private final Set<String> excludeViews;
	private final Map<String, DatasetResource> declaredViews;
	private final Map<String, Column> declaredColumns;
	private final TypeRegistry registry;

	//TODO allowLiteralJoin, allowLiteralQuery, ..
	
	public RequestContext(Resource schema, DatasetResource defaultView) {
		this(schema, defaultView, emptySet(), new HashMap<>(), new HashMap<>(), new TypeRegistry());
	}

	public RequestContext(Resource schema, DatasetResource defaultView, TypeRegistry registry) {
		this(schema, defaultView, emptySet(), new HashMap<>(), new HashMap<>(), registry);
	}
	
	public Optional<DatasetResource> lookupView(boolean allowParameterize, String name, Entry... args) { 
		var view = declaredViews.get(name);
		if(isNull(view) && !excludeViews.contains(name)) {
			try {
				return lookupSchemaResource(name, DatasetResource.class, args);
			}
			catch (EntryParseException e) {
				if(!allowParameterize && nonNull(args)) {
					throw new IllegalArgumentException("view resource '" + name + "' expects parameters, but parameterization is not allowed in this context");
				}
			}
			return Optional.empty();
		}
		return Optional.of(view);
	}

	public <T> Optional<T> lookupSchemaResource(String name, Class<T> type, Entry... args) { 
		return lookupResource(schema, name, type, args);
	}
	
	public <T> Optional<T> lookupViewResource(Resource view, String name, Class<T> type, Entry... args) { 
		return lookupResource(view, name, type, args);
	}
	
	<T> Optional<T> lookupResource(Resource resource, String name, Class<T> type, Entry... args) { 
		return resource.exposes(name, type)
				? Optional.of(resource.invokeResource(name, type, args, this))
				: Optional.empty();
	}
	
	void declareView(String name, DatasetResource view) {
		declaredViews.compute(name, (k,v)->{
			if(isNull(v)) {
				return view;
			}
			throw new IllegalArgumentException("a view with name '" + name + "' already exists in context");
		});
	}
	
	void declareColumn(String name, Column column) {
		declaredColumns.compute(name, (k,v)->{
			if(isNull(v)) {
				return column;
			}
			throw new IllegalArgumentException("a column with name '" + name + "' already exists in context");
		});
	}
	
	public Object resolve(Entry entry, JavaType... types) {
		if(entry.isVariable() && (isEmpty(types) || Stream.of(types).allMatch(JDBCType.class::isInstance))) {
			try {
				return resolve(entry, Column.class);
			}
			catch (NoSuchResourceException e) { //TODO check there's no other exception type to catch, otherwise we may hide a parsing error
				try {
					return resolve(entry, QueryView.class);
				}
				catch (NoSuchResourceException ex) { //TODO  check there's no other exception type to catch, otherwise we may hide a parsing error
					//do nothing, try other types
				}
			}
		}
		if(isEmpty(types)) {
			types = STD_TYPES;
		}
		for(var t : types) {
			try {
				return resolve(entry, t.getCorrespondingClass());
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

	@SuppressWarnings("unchecked")
	public <T> T[] resolveAll(Entry[] args, Class<T> type) {
		T[]	res = null;
		if(nonNull(args)) {
			res = (T[]) newInstance(type, args.length);
			for(int i=0; i<args.length; i++) {
				res[i] = resolve(args[i], type);
			}
		}
		return res;
	}

	public <T> T resolve(Entry entry, Class<T> type) {
		if(entry.isVariable()) {
			var prs = registry.getEvaluator(type);
			if(nonNull(prs)) {
				try {
					return prs.evaluate(entry, this);
				}
				catch (Exception e) {
					throw new EntryParseException("cannot evaluate '" + type.getSimpleName() + "' expression " + entry, e);
				}
			}
		}// consider variable with no args, next or tag as value entry
		if(!entry.hasArgs() && !entry.hasNext() && !entry.hasTag()) { 
			return evalValue(entry.getValue(), type);
		}
		throw new IllegalArgumentException("cannot resolve entry " + entry + " as type " + type.getSimpleName());
	}
	
	public <T> T evalValue(String value, Class<T> type) {
		var prs = registry.getParser(type);
		if(nonNull(prs)) {
			try {
				return prs.parse(value);
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse '" + type.getSimpleName() + "' value " + value, e);
			}
		}
		throw new NoSuchElementException("no parser for type " + type.getSimpleName());
	}

	public RequestContext subContext(DatasetResource view) {
		return new RequestContext(schema, view, excludeViews, new HashMap<>(), new HashMap<>(), registry);
	}
	
	public RequestContext withView(DatasetResource view) { //inherit cache
		if(view == defaultView) {
			return this;
		}
		return new RequestContext(schema, view, excludeViews, declaredViews, declaredColumns, registry);
	}
}
