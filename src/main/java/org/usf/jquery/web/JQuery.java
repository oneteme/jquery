package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElseGet;
import static org.usf.jquery.core.Utils.computeIfAbsentElseThrow;
import static org.usf.jquery.core.Utils.requireNonNullElseThrow;
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
	private static final ThreadLocal<Environment> LOCAL_ENV = new ThreadLocal<>();
	
	private static RequestParser requestParser = new DefaultRequestParser();

	public static void register(@NonNull Environment... envs) {
		for(var env : envs) {
			var id = env.getDatabase().identity();
			DATABASES.compute(id, 
					computeIfAbsentElseThrow(env, ()-> "environment already registered: " + id))
			.bind(); // bind the environment to the database after put
		}
	}

	public static Environment currentEnvironment() {
		return requireNonNullElseGet(LOCAL_ENV.get(), JQuery::defaultEnvironment);
	}
	
	public static Environment getEnvironment(String name) {
		return requireNonNullElseThrow(DATABASES.get(name), 
				()-> noSuchResourceException("environment", name));
	}

	public static Environment defaultEnvironment(){
		if(DATABASES.size() == 1) {
			return DATABASES.values().iterator().next();
		}
		throw noSuchResourceException("default environment");
	}

	public static <T> T apply(Function<Environment, T> fn) {
		return apply(defaultEnvironment(), fn);
	}
	
	public static <T> T apply(Environment env, Function<Environment, T> fn) {	
		var cur = LOCAL_ENV.get();
		if(isNull(cur)) {
			LOCAL_ENV.set(env);
			try {
				return fn.apply(env);
			} finally {
				LOCAL_ENV.remove();
			}
		}
		else if(cur.getDatabase() == env.getDatabase()) {
			return fn.apply(cur); //existing current env
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
