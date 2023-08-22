package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.web.ParsableJDBCType.AUTO_TYPE;

import org.usf.jquery.core.SQLType;

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
public class ColumnMetadata {
	
	private final String columnName;
	private SQLType dataType = AUTO_TYPE;
	private int dataSize = UNLIMITED;
	
	ColumnMetadata reset() {
		this.dataType = AUTO_TYPE;
		this.dataSize = UNLIMITED;
		return this;
	}
}