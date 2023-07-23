package org.usf.jquery.web;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class TableDecoratorWrapper implements TableDecorator {
	
	@Delegate
	private final TableDecorator table;
	private final Map<ColumnDecorator, ColumnMetadata> columns;
	
	@Override
	public int columnType(ColumnDecorator desc) {
		if(columns.containsKey(desc)) {
			return columns.get(desc).dataType;
		}
		return table.columnType(desc);
	}
	
	@Override
	public int columnSize(ColumnDecorator desc) {
		if(columns.containsKey(desc)) {
			return columns.get(desc).dataSize;
		}
		return table.columnSize(desc);
	}
	
	@RequiredArgsConstructor
	class ColumnMetadata {
		private final int dataType;
		private final int dataSize;
	}

}
