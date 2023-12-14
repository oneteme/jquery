package org.usf.jquery.web;

import static org.usf.jquery.core.JDBCType.AUTO;
import static org.usf.jquery.core.Utils.UNLIMITED;

import org.usf.jquery.core.JavaType;

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
	private JavaType dataType = AUTO;
	private int dataSize = UNLIMITED;
	
	ColumnMetadata reset() {
		this.dataType = AUTO;
		this.dataSize = UNLIMITED;
		return this;
	}
}