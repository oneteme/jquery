package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.join;

import java.util.stream.Stream;

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
	private final ArgTypeRef typeRef;
	private final boolean required;
	private final boolean varargs;

	public boolean accept(int idx, Object[] args) {
		var arr = types(args);
		return isEmpty(arr) || Stream.of(arr).anyMatch(t-> t.accept(args[idx]));
	}
	
	public JavaType[] types(Object[] args) {
		if(isNull(typeRef)) {
			return types;
		}
		var t = typeRef.apply(args); //nullable
		return isNull(t) ? null : new JavaType[] {t};
	}
	
	@Override
	public String toString() {
		if(isNull(typeRef)) {
			return isEmpty(types) ? "ANY" : join("|", types);
		}
		return typeRef.toString();
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
	
	public static Parameter required(ArgTypeRef typeRef) {
		return new Parameter(null, typeRef, true, false);
	}

	public static Parameter optional(ArgTypeRef typeRef) {
		return new Parameter(null, typeRef, false, false);
	}
	
	public static Parameter varargs(ArgTypeRef typeRef) {
		return new Parameter(null, typeRef, false, true);
	}
}
