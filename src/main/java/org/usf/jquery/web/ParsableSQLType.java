package org.usf.jquery.web;

import org.usf.jquery.core.JavaType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
public interface ParsableSQLType extends JavaType, ArgumentParser {
	
	public static ParsableSQLType unparsableType(int type) {
		return new UnparsableJDBCType(type, Object.class);
	}
	
	public static ParsableSQLType unparsableType(JavaType type) {
		return new UnparsableJDBCType(type.getValue(), type.type());
	}

	@Getter
	@RequiredArgsConstructor
	class UnparsableJDBCType implements ParsableSQLType {
		
		private final int value;
		private final Class<?> javaType;
		
		@Override
		public Object nativeParse(String v) {
			throw new UnsupportedOperationException("unsupported SQLType=" + value);
		}
	}
}
