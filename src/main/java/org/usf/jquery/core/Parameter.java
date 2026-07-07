package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isEmpty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Parameter {
	
	private final JavaType[] types; //empty => accept all types
	private final TypeResolver typeRef;
	private final boolean required;
	private final boolean varargs;

	public boolean accept(int idx, Object[] args) {
		var arr = types(args);
		return isEmpty(arr) || stream(arr).anyMatch(t-> t.accept(args[idx]));
	}
	
	public JavaType[] types(Object[] args) {
		if(nonNull(typeRef)) {
			var t = typeRef.apply(args); //nullable
			return nonNull(t) ? new JavaType[] {t} : null;
		}
		return types;
	}
	
	@Override
	public String toString() {
		if(nonNull(typeRef)) {
			return typeRef.toString();
		}
		return toString(types);
	}
	
	public static Parameter required(JavaType... types) {
		return new Parameter(types, null, true, false);
	}

	public static Parameter optional(JavaType... types) {
		return new Parameter(types, null, false, false);
	}
	
	public static Parameter varargs(JavaType... types) {
		return new Parameter(types, null, false, true);
	}
	
	public static Parameter required(TypeResolver typeRef) {
		return new Parameter(null, typeRef, true, false);
	}

	public static Parameter optional(TypeResolver typeRef) {
		return new Parameter(null, typeRef, false, false);
	}
	
	public static Parameter varargs(TypeResolver typeRef) {
		return new Parameter(null, typeRef, false, true);
	}
	
	public static boolean match(Object o, JavaType... types) {
		return isEmpty(types) || stream(types).anyMatch(t-> t.accept(o));
	}
	
	public static String toString(JavaType[] types) {
		return isEmpty(types) 
				? "<ANY>" 
				: stream(types).map(Object::toString).collect(joining("|"));
	}
}
