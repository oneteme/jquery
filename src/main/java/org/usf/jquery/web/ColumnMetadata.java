package org.usf.jquery.web;

import static java.lang.Integer.MAX_VALUE;
import static org.usf.jquery.core.JDBCType.DECIMAL;
import static org.usf.jquery.core.JDBCType.DOUBLE;
import static org.usf.jquery.core.JDBCType.FLOAT;
import static org.usf.jquery.core.JDBCType.NUMERIC;
import static org.usf.jquery.core.JDBCType.REAL;
import static org.usf.jquery.core.Utils.UNLIMITED;

import java.sql.Timestamp;

import org.usf.jquery.core.JDBCType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author u$f
 * 
 */
@ToString
@Getter
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor
public final class ColumnMetadata {
	
	private final String columnName;
	private JDBCType dataType = null;
	private int dataSize = UNLIMITED;
	private Integer precision = UNLIMITED;
	
	ColumnMetadata reset() {
		this.dataType  = null;
		this.dataSize  = UNLIMITED;
		this.precision = UNLIMITED;
		return this;
	}

	public String toJavaType(){
		return dataType.type().getSimpleName();
	}
	
	public String toSqlType(){
		var s = dataType.name();
		if(dataType.type() == String.class) {
			s+= "(" + (dataSize == MAX_VALUE ? "" : dataSize) + ")";
		}
		if(dataType.type() == Timestamp.class) {
			s+= "(" + precision + ")";
		}
		if(dataType == REAL || dataType == NUMERIC || dataType == DECIMAL || dataType == FLOAT || dataType == DOUBLE) {
			s+= "(" + dataSize + "," + precision + ")";
		}
		return s;
	}
}