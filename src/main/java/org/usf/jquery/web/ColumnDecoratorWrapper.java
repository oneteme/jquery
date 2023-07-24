package org.usf.jquery.web;

import static java.util.Objects.isNull;

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
	private ArgumentParser parser;
	//index column ?
	//cache values !? more performance
	
	@Override
	public ArgumentParser parser(int type) { //cache parser
		if(isNull(parser)) {
			parser = column.parser(type); //important! lazy load
		}
		return parser;
	}
	
	public ColumnDecorator unwrap() {
		return column;
	}
}
