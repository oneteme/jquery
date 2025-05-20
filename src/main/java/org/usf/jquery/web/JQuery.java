package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 
 * @author u$f
 * 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JQuery {

	private static final Map<String, Environment> DATABASES = new HashMap<>();
	private static final ThreadLocal<ExecutionContext> LOCAL_CONTEXT = new ThreadLocal<>();
	
	private static RequestParser requestParser = new DefaultRequestParser();

	public static void register(@NonNull Environment... envs) {
		for(var env : envs) {
			DATABASES.compute(env.getDatabase().identity(), (k,v)-> {
				if(isNull(v)) {
					return env;
				}
				throw new IllegalArgumentException("environment already registered: " + k);
			});
			env.bind(); // outer bind
		}
	}
	
	public static Environment getEnvironment(String name) {
		var env = DATABASES.get(name);
		if(nonNull(env)) {
			return env;
		}
		throw noSuchResourceException("environment", name);
	}

	public static Environment defaultEnvironment(){
		if(DATABASES.size() == 1) {
			return DATABASES.values().iterator().next();
		}
		throw noSuchResourceException("default environment");
	}

	public static ExecutionContext currentContext() {
		var ctx = LOCAL_CONTEXT.get();
		return nonNull(ctx)
				? ctx
				: new ExecutionContext(defaultEnvironment());
	}

	public static <T> T context(Function<ExecutionContext, T> fn) {
		return context(defaultEnvironment(), fn);
	}
	
	public static <T> T context(Environment env, Function<ExecutionContext, T> fn) {
		var ctx = LOCAL_CONTEXT.get();
		if(isNull(ctx)) {
			ctx = new ExecutionContext(env);
			LOCAL_CONTEXT.set(ctx);
			try {
				return fn.apply(ctx);
			} finally {
				LOCAL_CONTEXT.remove();
			}
		}
		else if(ctx.getEnvironment().getDatabase() == env.getDatabase()) {
			return fn.apply(ctx);
		}
		throw new UnsupportedOperationException("cannot use different environment in the same thread");
	}

	public static RequestParser getRequestParser() {
		return requestParser;
	}

	//customize the request parser
	public static void setRequestParser(@NonNull RequestParser requestParser) {
		JQuery.requestParser = requestParser;
	}
}
