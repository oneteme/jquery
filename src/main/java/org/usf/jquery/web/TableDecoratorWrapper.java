package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.AUTO_TYPE;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class TableDecoratorWrapper implements TableDecorator {
	
	@Delegate
	private final TableDecorator table;
	private final Map<String, ColumnMetadata> columns;
	
	@Override
	public int columnType(ColumnDecorator desc) {
		var type = table.columnType(desc); //overridden
		if(type == AUTO_TYPE && columns.containsKey(desc.identity())) {
			type = columns.get(desc.identity()).dataType;
		}
		return type;
	}
	
	@Override
	public int columnSize(ColumnDecorator desc) {
		var size = table.columnSize(desc); //overridden
		if(size == AUTO_TYPE && columns.containsKey(desc.identity())) {
			size = columns.get(desc.identity()).dataSize;
		}
		return size;
	}
	
	@RequiredArgsConstructor
	static final class ColumnMetadata {
		private final String name;
		private final int dataType;
		private final int dataSize;
	}

}
