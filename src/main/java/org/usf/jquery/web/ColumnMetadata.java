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

import org.usf.jquery.core.ViewColumn;

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
	
	private ViewColumn column;
	private int dataSize;
	private int precision;
	private final boolean overConfigured;
	
	@Deprecated
	ColumnMetadata reset() {
		this.dataSize  = UNLIMITED;
		this.precision = UNLIMITED;
		return this;
	}
	
	public void update(int type, int size, int precision) {
		if(!overConfigured) {
			var ct = fromDataType(type).orElse(OTHER);
			if(ct != column.getType()) {
				column = new ViewColumn(column.getView(), column.getName(), column.getTag(), ct);
			}
			this.dataSize = size;
			this.precision = precision;
		}
	}
	
	public String toJavaType(){
		return column.getType().typeClass().getSimpleName();
	}
	
	public String toSqlType(){
		var dataType = column.getType();
		var s = dataType.name();
		if(!overConfigured) {
			if(dataType.typeClass() == String.class && dataSize < MAX_VALUE) {
				s+= "(" + dataSize + ")";
			}
			if(dataType.typeClass() == Timestamp.class) {
				s+= "(" + precision + ")";
			}
			if(dataType == REAL || dataType == NUMERIC || dataType == DECIMAL || dataType == FLOAT || dataType == DOUBLE) {
				s+= "(" + dataSize + "," + precision + ")";
			}
		}
		return s;
	}
	
	public static ColumnMetadata columnMetadata(ViewColumn col) {
		return new ColumnMetadata(col, UNLIMITED, UNLIMITED, nonNull(col.getType()));
	}
}