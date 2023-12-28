package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.JDBCType.AUTO;
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
	
	private final JavaType[] types;
	private final boolean required;
	private final boolean varargs;
	
	public JavaType[] getTypes() {
		return isEmpty(types) ? new JavaType[] {AUTO} : types;
	}

	public boolean accept(Object o) {
		return isEmpty(types) || Stream.of(types).anyMatch(t-> t.accept(o));
	}
	
	@Override
	public String toString() {
		return Stream.of(types)
				.map(Object::toString)
				.collect(joining("|"));
	}
	
	public static Parameter required(JavaType... types) {
		return new Parameter(types, true, false);
	}

	public static Parameter optional(JavaType... types) {
		return new Parameter(types, false, false);
	}
	
	public static Parameter varargs(JavaType... types) {
		return new Parameter(types, false, true);
	}
	
	public static Parameter[] checkArgs(Parameter... parameters) {
		if(nonNull(parameters)) {
			var i = parameters.length;
			while(--i>=0 && parameters[i].isRequired());
			for(; i>=0; i--) {
				if(parameters[i].isRequired()) {
					throw new IllegalArgumentException("optional argument");
				}
			}
		}
		return parameters;
	}
}