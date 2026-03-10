package org.usf.jquery.web.proxy;

import static java.lang.reflect.Array.newInstance;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptySet;
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
import static org.usf.jquery.core.Parameter.match;
import static org.usf.jquery.core.Signature.badArgumentTypeException;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.proxy.Resource.Match.HIDDEN;
import static org.usf.jquery.web.proxy.Resource.Match.VALID;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Definition;
import org.usf.jquery.core.Dialect;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.SignatureMismatchException;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.EntrySyntaxException;
import org.usf.jquery.web.NoSuchResourceException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class RequestContext {

	private static final JDBCType[] STD_TYPES = { 
			BIGINT, DOUBLE, DATE, TIMESTAMP, 
			TIME, TIMESTAMP_WITH_TIMEZONE, VARCHAR };

	@Getter
	private final DatasetResource defaultDataset;
	private final StoreResource store;
	private final Set<String> excludeViews;
	private final Map<String, DatasetResource> declaredViews;
	private final Map<String, Column> declaredColumns;
	private final TypeRegistry registry;

	// SecurityPolicy(allowLiteralJoin, allowLiteralQuery, ..)
	
	public RequestContext(StoreResource store, DatasetResource defaultDataset, TypeRegistry registry) {
		this(defaultDataset, store, emptySet(), new HashMap<>(), new HashMap<>(), registry);
	}
	
	public Dialect getDialect(){
		return store.dialect();
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
	
	public Optional<Column> lookupDeclaredColumn(String name) {
		return ofNullable(declaredColumns.get(name));
	}
	
	public <T> Optional<T> lookupDialectResource(String name, Class<T> type) {
		return lookupDialectResource(name, type, null);
	}
	
	public <T> Optional<T> lookupDialectResource(String name, Class<T> type, Object composer) {
		var match = store.exposes(name, type); //cannot override composer method
		if(match == VALID) {
			return lookupSchemaResource(name, type);
		}
		if(match == HIDDEN) {
			log.warn("resource '{}' of type {} is hidden by store, cannot be used", name, type.getSimpleName());
			return empty();
		}
		try {
			var mth = nonNull(composer) 
					? store.dialect().getClass().getMethod(name, composer.getClass())
					: store.dialect().getClass().getMethod(name); //no parameter
			if(nonNull(mth)) {
				var mod = mth.getModifiers();
				var npr = nonNull(composer) ? 1 : 0;
				if(type.isAssignableFrom(mth.getReturnType()) && mth.getParameterCount()== npr && isPublic(mod) && !isStatic(mod)) {
					var res = nonNull(composer) 
							? mth.invoke(store.dialect(), composer)
							: mth.invoke(store.dialect());
					return Optional.of(type.cast(res));
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.warn("failed to invoke method '{}' of type {} for lookup, reason: {}", name, type.getSimpleName(), e.getMessage());
		}
		catch (Exception e) { //NoSuchMethodException, SecurityException
			//do nothing, return empty
		}
		return empty();
	}

	public <T> Optional<T> lookupSchemaResource(String name, Class<T> type, Entry... args) { 
		return lookupResource(store, name, type, args);
	}
	
	public <T> Optional<T> lookupViewResource(Resource view, String name, Class<T> type, Entry... args) { 
		return lookupResource(view, name, type, args);
	}
	
	<T> Optional<T> lookupResource(Resource resource, String name, Class<T> type, Entry... args) { 
		return resource.exposes(name, type) == VALID
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
	
	public Object[] resolveArgs(Entry[] args, Object res, Definition<?> def){ //TODO parse on demand
		int shift = nonNull(res) ? 1 : 0;
		try {
			return def.getSignature().buildArgs(shift + (nonNull(args) ? args.length : 0), (idx,types)-> {
				if(idx==0 && nonNull(res)) {
					if(match(res, types)) {
						return res;
					}
					throw badArgumentTypeException(res, types);
				}
				else {
					return resolveEntry(args[idx-shift], types);
				}
			});
		}
		catch (SignatureMismatchException e) {
			throw new EntryParseException("cannot resolve arguments for " + def, e);
		}
	}
	
	public Object resolveEntry(Entry entry, JavaType... types) {
		if(entry.isVariable() && (isEmpty(types) || Stream.of(types).allMatch(JDBCType.class::isInstance))) {
			try {
				return resolve(entry, Column.class);
			}
			catch (NoSuchResourceException e) {
				try {
					return resolve(entry, SingleQueryColumn.class);
				}
				catch (NoSuchResourceException ex) {
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
	public <T> T resolveArray(Entry[] args, Class<T> type) {
		if(nonNull(args)) {
			if(type.isArray()) {
				var arr = newInstance(type.componentType(), args.length);
				for(int i=0; i<args.length; i++) {
					Array.set(arr, i, resolve(args[i], type.componentType()));
				}
				return (T)arr;
			}
			if(args.length == 1) {
				return resolve(args[0], type);
			}
			if(args.length > 1) {
				throw new EntrySyntaxException("multiple entries cannot be resolved to single value of type " + type.getSimpleName());
			}
		}
		return null;
	}

	public <T> T resolve(Entry entry, Class<T> type) {
		if(entry.isVariable()) {
			var prs = registry.getEvaluator(type);
			if(nonNull(prs)) {
//				try {
					return prs.evaluate(entry, this);
//				}
//				catch (Exception e) { this hide original exception
//					throw new EntryParseException("cannot evaluate '" + type.getSimpleName() + "' expression " + entry, e);
//				}
			}
		}// consider variable with no args, next or tag as value entry
		if(!entry.hasArgs() && !entry.hasNext() && !entry.hasTag()) { 
			return parseValue(entry.getValue(), type);
		}
		throw new EntryParseException("cannot resolve entry " + entry + " to type " + type.getSimpleName());
	}
	
	public <T> T parseValue(String value, Class<T> type) {
		var prs = registry.getParser(type);
		if(nonNull(prs)) {
			try {
				return nonNull(value) ? prs.parse(value) : null;
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse '" + type.getSimpleName() + "' value " + value, e);
			}
		}
		throw new EntryParseException("no parser for type " + type.getSimpleName());
	}

	//inherit common properties, but not declared views and columns
	public RequestContext subContext(DatasetResource dataset) {
		return new RequestContext(dataset, store, excludeViews, new HashMap<>(), new HashMap<>(), registry);
	}
	
	//inherit declared views and columns, but with different default view
	public RequestContext withView(DatasetResource dataset) { 
		if(dataset == defaultDataset) {
			return this;
		}
		return new RequestContext(dataset, store, excludeViews, declaredViews, declaredColumns, registry);
	}
}
