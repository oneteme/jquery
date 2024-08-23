package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.Parameters.DATABASE;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextManager {

	private static final Map<String, ContextEnvironment> CONTEXTS = new HashMap<>();
	private static final ThreadLocal<ContextEnvironment> CURRENT  = new ThreadLocal<>();

	public static void register(ContextEnvironment config) {
		CONTEXTS.compute(config.getDatabase().identity(), (k,v)-> {
			if(isNull(v)) {
				return config;
			}
			throw resourceAlreadyExistsException(k, v);
		});
		config.bind(); // outer bind
	}

	public static ContextEnvironment currentContext() {
		var ctx = CURRENT.get();
		if(nonNull(ctx)) {
			return ctx;
		}
		if(CONTEXTS.size() == 1) { //default database
			ctx = CONTEXTS.values().iterator().next();
			return setCurrentContext(new ContextEnvironment(ctx));
		}
		throw CONTEXTS.isEmpty()
			? new NoSuchElementException("no database configured")
			: new IllegalStateException("no database selected");
	}

	static ContextEnvironment context(String database){
		var ctx = CONTEXTS.get(database);
		if(nonNull(ctx)) {
			return setCurrentContext(new ContextEnvironment(ctx));
		}
		throw noSuchResourceException(DATABASE, database);
	}

	static ContextEnvironment setCurrentContext(ContextEnvironment ctx) {
		CURRENT.set(ctx);
		return ctx;
	}

	static void releaseContext() {
		CURRENT.remove();
	}
}
