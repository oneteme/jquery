package org.usf.jquery.web;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.DECIMAL;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.FLOAT;
import static org.usf.jquery.core.JDBCType.NUMERIC;
import static org.usf.jquery.core.JDBCType.OTHER;
import static org.usf.jquery.core.JDBCType.REAL;
import static org.usf.jquery.core.JDBCType.fromDataType;
import static org.usf.jquery.core.Utils.UNLIMITED;

import java.sql.Timestamp;

import org.usf.jquery.core.JDBCType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author u$f
 * 
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnMetadata {

	private final String name;
	private JDBCType type;
	private int dataSize;
	private int precision;
	private final boolean overConfigured;
	
	public void update(int type, int size, int precision) {
		if(!overConfigured) {
			this.type = fromDataType(type).orElse(OTHER);
			this.dataSize = size;
			this.precision = precision;
		}
	}
	
	ColumnMetadata reset() {
		if(!overConfigured) {
			this.type = null;
			this.dataSize  = UNLIMITED;
			this.precision = UNLIMITED;
		}
		return this;
	}
	
	public String toJavaType(){
		var t = type.getCorrespondingClass().getSimpleName();
		return overConfigured ? t+"!" : t;
	}
	
	public String toSqlType(){
		var s = type.name();
		if(overConfigured) {
			s+="!";
		}
		else {
			if(type.getCorrespondingClass() == String.class && dataSize < MAX_VALUE) {
				s+= "(" + dataSize + ")";
			}
			if(type.getCorrespondingClass() == Timestamp.class) {
				s+= "(" + precision + ")";
			}
			if(type == REAL || type == NUMERIC || type == DECIMAL || type == FLOAT || type == DOUBLE) {
				s+= "(" + dataSize + "," + precision + ")";
			}
		}
		return s;
	}
	
	public static ColumnMetadata columnMetadata(String name, JDBCType type) {
		return new ColumnMetadata(name, type, UNLIMITED, UNLIMITED, nonNull(type));
	}
}