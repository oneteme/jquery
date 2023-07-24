package org.usf.jquery.web;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
final class ColumnMetadata {
	
	private final String columnName;
	private final int dataType;
	private final int dataSize;
}