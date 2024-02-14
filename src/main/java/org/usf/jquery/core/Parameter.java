package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isEmpty;

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
		return nonNull(typeRef) 
				? typeRef.apply(args).accept(args[idx])
				: isEmpty(types) || Stream.of(types).anyMatch(t-> t.accept(args[idx]));
	}
	
	public JavaType[] types(Object[] args) {
		return nonNull(typeRef) 
				? new JavaType[] {typeRef.apply(args)}
				: types;
	}
	
	@Override
	public String toString() {
		if(nonNull(typeRef)) {
			return typeRef.toString();
		}
		return isEmpty(types) 
				? "ANY" 
				: Stream.of(types).map(Object::toString).collect(joining("|"));
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
