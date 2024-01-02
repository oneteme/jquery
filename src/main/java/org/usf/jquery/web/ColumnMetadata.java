package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.UNLIMITED;

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
	
	ColumnMetadata reset() {
		this.dataType = null;
		this.dataSize = UNLIMITED;
		return this;
	}
}