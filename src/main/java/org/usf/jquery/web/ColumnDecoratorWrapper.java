package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.UNLIMITED;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class ColumnDecoratorWrapper implements ColumnDecorator {

	@Delegate
	private final ColumnDecorator column;
	private final int type;
	private final int size;
	private ArgumentParser parser;
	//cache values !? more performance
	
	public ColumnDecoratorWrapper(ColumnDecorator column) {
		this(column, column.dbType(), UNLIMITED); //unknown size
	}
	
	@Override
	public int dbType() {
		return type;
	}

	@Override
	public int dataSize() {
		return size;
	}
	
	@Override
	public ArgumentParser parser() {
		if(isNull(parser)) {
			parser = column.parser(); //important! lazy load
		}
		return parser;
	}
}
