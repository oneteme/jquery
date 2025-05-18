package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
	private static final ThreadLocal<QueryContext> LOCAL_CONTEXT = new ThreadLocal<>();
	
	private static RequestParser requestParser = new DefaultRequestParser();

	public static void register(@NonNull Environment... envs) {
		for(var env : envs) {
			DATABASES.compute(env.getDatabase().identity(), (k,v)-> {
				if(isNull(v)) {
					return env;
				}
				throw new IllegalArgumentException(k + " already registered");
			});
			env.bind(); // outer bind
		}
	}

	public static Optional<Environment> lookupDatabase(){
		return DATABASES.size() == 1
				? Optional.of(DATABASES.values().iterator().next())
				: empty();
	}

	public static Optional<Environment> lookupDatabase(@NonNull String database){
		return ofNullable(DATABASES.get(database));
	}

	public static QueryContext currentContext() {
		return requireNonNull(LOCAL_CONTEXT.get(), "currentContext");
	}

	static void setCurrentContext(QueryContext ctx) {
		LOCAL_CONTEXT.set(ctx);
	}

	static void releaseContext() {
		LOCAL_CONTEXT.remove();
	}
	
	public static RequestParser getRequestParser() {
		return requestParser;
	}
	
	//customize the request parser
	public static void setRequestParser(@NonNull RequestParser requestParser) {
		JQuery.requestParser = requestParser;
	}
}
