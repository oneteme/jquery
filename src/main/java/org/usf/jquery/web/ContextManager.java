package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.quote;

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
		CONTEXTS.compute(config.getDatabase().identity(), (id,dm)-> {
			if(isNull(dm)) {
				return config;
			}
			throw new IllegalStateException("context environement conflict " + quote(id));
		});
	}

	public static ContextEnvironment currentContext() {
		var ctx = CURRENT.get();
		if(nonNull(ctx)) {
			return ctx;
		}
		if(CONTEXTS.size() == 1) { //default database
			return setCurrentContext(CONTEXTS.values().iterator().next());
		}
		throw CONTEXTS.isEmpty()
			? new NoSuchElementException("no database configured")
			: new IllegalStateException("no database selected");
	}

	static ContextEnvironment context(String database){
		var ctx = CONTEXTS.get(database);
		if(nonNull(ctx)) {
			return setCurrentContext(ctx);
		}
		throw new NoSuchElementException(database);
	}

	static ContextEnvironment setCurrentContext(ContextEnvironment ctx) {
		ctx = new ContextEnvironment(ctx); //copy
		CURRENT.set(ctx);
		return ctx;
	}

	static void releaseContext() {
		CURRENT.remove();
	}
}