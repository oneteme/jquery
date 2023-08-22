package org.usf.jquery.web;

import org.usf.jquery.core.SQLType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface ParsableSQLType extends SQLType, ArgumentParser {
	
	public static ParsableSQLType unparsableType(int type) {
		return new UnparsableJDBCType(type, Object.class);
	}
	
	public static ParsableSQLType unparsableType(SQLType type) {
		return new UnparsableJDBCType(type.getValue(), type.getJavaType());
	}

	@Getter
	@RequiredArgsConstructor
	public class UnparsableJDBCType implements ParsableSQLType {
		
		private final int value;
		private final Class<?> javaType;
		
		@Override
		public Object nativeParse(String v) {
			throw new UnsupportedOperationException("unsupported SQLType=" + value + " parse");
		}
	}
}
