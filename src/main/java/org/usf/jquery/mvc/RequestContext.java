package org.usf.jquery.mvc;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
import static org.usf.jquery.core.JDBCType.DATE;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.TIME;
import static org.usf.jquery.core.JDBCType.TIMESTAMP;
import static org.usf.jquery.core.JDBCType.TIMESTAMP_WITH_TIMEZONE;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JQueryType.DECLARE_COLUMN;
import static org.usf.jquery.core.Parameter.match;
import static org.usf.jquery.core.Signature.badArgumentTypeException;
import static org.usf.jquery.core.Utils.isEmpty;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Definition;
import org.usf.jquery.core.Dialect;
import org.usf.jquery.core.InvocationException;
import org.usf.jquery.core.JDBCType;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.SignatureMismatchException;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.ViewColumn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RequestContext {

	private static final JDBCType[] STD_TYPES = { 
			BIGINT, DOUBLE, DATE, TIMESTAMP, TIME, 
			TIMESTAMP_WITH_TIMEZONE, BOOLEAN, VARCHAR };

	@Getter private final DatasetCatalog defaultDataset;
	@Getter private final StoreCatalog store; 
	private final TypeRegistry registry;

	private final Map<String, DatasetCatalog> declaredViews;
	private final Map<String, Column> declaredColumns;
	//strict mode !? resolve order, groupBy, ..
	
	public RequestContext(StoreCatalog store, DatasetCatalog defaultDataset, TypeRegistry registry) {
		this(defaultDataset, store, registry, new HashMap<>(), new HashMap<>());
	}
	
	public Dialect getDialect(){
		return store.dialect();
	}
	
	public DatasetCatalog lookupView(String name, boolean allowParametred, Entry... args) { 
		return declaredViews.compute(name, (k,v)->{
			if(isNull(v)) {
				var inv = store.lookup(name, DatasetCatalog.class);
				if(nonNull(inv)) {
					if(allowParametred || isEmpty(inv.getParameters())) {
						v = invokeResource(inv, args);
					}
					else {
						throw new InvocationException("parameterized view '%s' not allowed in this context".formatted(name));
					}
				}
			}
			return v;
		});
	}
	
	public <T> T lookupResource(String name, DatasetCatalog view, Class<T> type, Entry... args) {
		var res = store.lookup(view, name, type);
		return nonNull(res) ? invokeResource(res, args) : null;
	}
	
	public Optional<Column> lookupDeclaredColumn(String name) {
		return ofNullable(declaredColumns.get(name));
	}
	
	public Object lookupDialect(String name, Class<? extends Definition> type, Object composer, Entry... args) {
		var inv = store.lookupDialect(name, type, composer);
		if(nonNull(inv)) {
			var def = nonNull(composer) ? inv.invoke(composer) : inv.invoke();
			return def.invoke(resolveArgs(args, null, def));
		}
		return null;
	}
	
	void declareView(String name, DatasetCatalog view) {
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
	
	public <T> T invokeResource(ResourceInvoker<T> invok, Entry... args) {
		return invok.invoke(evaluate(args, invok.getParameters()));
	}
	
	Object[] evaluate(Entry[] args, Class<?>[] params) {
		var nArgs = nonNull(args) ? args.length : 0;
		if(params.length == 1 && params[0].isArray()) {
			var type = params[0].getComponentType();
			var arr = newInstance(type, nArgs);
			for(int i=0; i<nArgs; i++) {
				Array.set(arr, i, resolve(args[i], type));
			}
			return new Object[] {arr}; //allow empty array ?
		}
		if(params.length == nArgs) {
			var arr = new Object[nArgs];
			for(int i=0; i<nArgs; i++) {
				arr[i] = resolve(args[i], params[i]);
			}
			return arr;
		}
		throw new IllegalArgumentException("expected " + params.length + " arguments but got " + nArgs);
	}
	
	public Object[] resolveArgs(Entry[] args, Object operand, Definition<?> def){ //TODO parse on demand
		int shift = nonNull(operand) ? 1 : 0;
		try {
			return def.getSignature().buildArgs(shift + (nonNull(args) ? args.length : 0), (idx,types)-> {
				if(idx==0 && nonNull(operand)) {
					if(match(operand, types)) {
						return operand;
					}
					throw badArgumentTypeException(operand, types);
				}
				return resolveEntry(args[idx-shift], types);
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
				log.trace("failed to resolve entry {} to type {}, reason: {}", entry, Column.class, e.getMessage());
				try {
					return resolve(entry, SingleQueryColumn.class);
				}
				catch (NoSuchResourceException ex) {
					log.trace("failed to resolve entry {} to type {}, reason: {}", entry, SingleQueryColumn.class, e.getMessage());
				}
			}
		}
		if(isEmpty(types)) {
			types = STD_TYPES;
		}
		for(var t : types) {
			try {
				return resolve(entry, t);
			}
			catch (Exception e) {
				if(types.length == 1) {
					throw e;
				}
				log.trace("failed to resolve entry {} to type {}, reason: {}", entry, t, e.getMessage());
			}
		}
		throw new NoSuchElementException("no parser for type " + Arrays.toString(types));
	}
	
	public Object resolve(Entry entry, JavaType type) {
		if(type == DECLARE_COLUMN && entry.isVariable()) {
			var col = registry.getEvaluator(Column.class).evaluate(entry, this);
			if(nonNull(col.getTag()) && !(col instanceof ViewColumn)) { //no need to declare view column, it is already declared in view
				declareColumn(col.getTag(), col);
			}
			return col;
		}
		return resolve(entry, type.getCorrespondingClass());
	}
	
	public <T> T resolve(Entry entry, Class<T> type) {
		if(entry.isVariable()) {
			var prs = registry.getEvaluator(type);
			if(nonNull(prs)) {
				return prs.evaluate(entry, this);
			}
			else if(type == JQueryType.class) {
				throw new UnsupportedOperationException("");
			}
		}
		return parseValue(entry, type); //string value can be considered as variable
	}
	
	public <T> T parseValue(Entry entry, Class<T> type) {
		if(!entry.hasArgs() && !entry.hasNext() && !entry.hasTag()) {
			var v = entry.getValue();
			if(nonNull(v)) {
				var prs = registry.getParser(type);
				if(nonNull(prs)) {
					try {
						return prs.parse(v);
					}
					catch (Exception e) {
						throw new EntryParseException("cannot parse '" + type.getSimpleName() + "' value " + v, e);
					}
				}
				throw new EntryParseException("no parser for type " + type.getSimpleName());
			}
			return null;
		}
		throw new EntryParseException("cannot resolve entry " + entry + " to type " + type.getSimpleName());
	}

	//inherit common properties, but not declared views and columns
	public RequestContext subContext(DatasetCatalog dataset) {
		return new RequestContext(dataset, store, registry, new HashMap<>(), new HashMap<>());
	}
	
	//inherit declared views and columns, but with different default view
	public RequestContext withView(DatasetCatalog dataset) { 
		if(dataset == defaultDataset) {
			return this;
		}
		return new RequestContext(dataset, store, registry, declaredViews, declaredColumns);
	}
}
