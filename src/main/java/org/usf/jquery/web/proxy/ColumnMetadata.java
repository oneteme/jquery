package org.usf.jquery.web.proxy;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.JDBCType.DECIMAL;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.FLOAT;
import static org.usf.jquery.core.JDBCType.NUMERIC;
import static org.usf.jquery.core.JDBCType.REAL;

import java.sql.Timestamp;

import org.usf.jquery.core.JDBCType;

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
@AllArgsConstructor
public final class ColumnMetadata {

	private final String name;
	private final JDBCType type;
	private final int dataSize;
	private final int precision;
	
	public String toJavaType(){
		return isNull(type) ? null : type.getCorrespondingClass().getSimpleName();
	}
	
	public String toSqlType(){
		var s = type.name();
		if(type.getCorrespondingClass() == String.class && dataSize < MAX_VALUE) {
			s+= "(" + dataSize + ")";
		}
		if(type.getCorrespondingClass() == Timestamp.class) {
			s+= "(" + precision + ")";
		}
		if(type == REAL || type == NUMERIC || type == DECIMAL || type == FLOAT || type == DOUBLE) {
			s+= "(" + dataSize + "," + precision + ")";
		}
		return s;
	}
}