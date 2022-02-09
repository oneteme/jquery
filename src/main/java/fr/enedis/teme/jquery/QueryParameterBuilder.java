package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;
import static fr.enedis.teme.jquery.Validation.illegalArgumentIfNot;
import static java.lang.reflect.Array.getLength;
import static java.util.stream.IntStream.range;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter(value = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryParameterBuilder {
	
	private static final QueryParameterBuilder STATIC_INSTANCE = new QueryParameterBuilder(null, false, false);
	private static final String ARG = "?";
	
	private final Collection<Object> args;
	private final boolean ps;
	private boolean dynamic;
	
	public String appendNullableParameter(Object o) {
		return o == null ? appendNull() : appendParameter(o);
	}

	public String appendParameter(@NonNull Object o) {
		illegalArgumentIf(o.getClass().isArray(), "array value");
		if(ps && dynamic) {
			args.add(o);
			return ARG;
		}
		return formatValue(o);
	}

	public String appendNullableString(Object o) {
		return o == null ? appendNull() : appendString(o);
	}
	
	public String appendString(@NonNull Object o) {
		illegalArgumentIfNot(o instanceof String, "not string");
		if(ps && dynamic) {
			args.add(o);
			return ARG;
		}
		return formatString(o); 
	}
	
	public String appendArray(@NonNull Object o) {
		illegalArgumentIf(!o.getClass().isArray(), "not array");
		illegalArgumentIf(getLength(o) == 0, "not array");
		if(ps && dynamic) {
			forEach(o, args::add);
			return nParameter(getLength(o));
		}
		var sb = new LinkedList<>();
		Function<Object, String> fn = arrayFormatter(o);
		forEach(o, v-> sb.add(fn.apply(v)));
		return String.join(",", sb.toArray(String[]::new));
	}
	
	private String appendNull() {
		if(ps && dynamic) {
			args.add(null);
			return ARG;
		}
		return "null";
	}

	public String staticMode(Supplier<String> supp) {
		this.dynamic = false;
		var v = supp.get();
		this.dynamic = true;
		return v;
	}
	
	static void forEach(Object o, Consumer<Object> cons) {
		
		range(0, getLength(o))
			.mapToObj(i-> Array.get(o, i))
			.forEach(cons);
	}

	static String nParameter(int n){
        return n == 1 ? ARG : ARG + ",?".repeat(n-1);
    }

	static String formatValue(Object o) {
		if(o == null) {
			return "null";
		}
		if(o instanceof Number || o.getClass().isPrimitive()) {
			return o.toString();
		}
		return formatString(o);
	}
	
	static String formatString(Object o) {
		return "'" + o + "'";
	}

	static Function<Object, String> arrayFormatter(Object arr) {
		var type = arr.getClass().getComponentType();
		return Number.class.isAssignableFrom(type) || type.isPrimitive()
				? Object::toString
				: QueryParameterBuilder::formatString;
	}
	
	public static QueryParameterBuilder addWithValue() {
		return STATIC_INSTANCE;
	}
	
	public static QueryParameterBuilder parametrized() {
		return new QueryParameterBuilder(new LinkedList<>(), true, true);
	}
}
