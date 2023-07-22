package org.usf.jquery.web;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TableMetadata {

	private final Map<ColumnDecorator, ColumnMetadata> columns;
	
	public ColumnMetadata column(ColumnDecorator cd) {
		return columns.get(cd);
	}
}
