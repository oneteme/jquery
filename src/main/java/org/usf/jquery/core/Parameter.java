package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.joinArray;

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
		return isEmpty(types) ? "ANY" : joinArray("|", types);
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
