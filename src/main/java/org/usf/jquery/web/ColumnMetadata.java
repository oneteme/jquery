package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter(value = AccessLevel.PACKAGE)
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
class ColumnMetadata {
	
	private final String columnName;
	private int dataType = AUTO_TYPE;
	private int dataSize = UNLIMITED;
	
	ColumnMetadata reset() {
		this.dataType = AUTO_TYPE;
		this.dataSize = UNLIMITED;
		return this;
	}
}