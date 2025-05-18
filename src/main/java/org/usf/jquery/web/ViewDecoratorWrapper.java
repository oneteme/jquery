package org.usf.jquery.web;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewJoin;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class ViewDecoratorWrapper implements ViewDecorator {

	//do no use @Delegate
	private final ViewDecorator view; 
	private final String id;
		
	@Override
	public String identity() {
		return id;
	}

	@Override
	public String columnName(ColumnDecorator cd) {
		return view.columnName(cd);
	}
	
//	@Override
//	public ViewBuilder builder() {
//		return ()-> view.builder().build()::build; //different reference
//	}
	
	@Override
	public Builder<DBFilter> criteria(String name) {
		return view.criteria(name);
	}
	
	@Override
	public Builder<ViewJoin[]> join(String name) {
		return view.join(name);
	}
	
	@Override
	public Builder<Partition> partition(String name) {
		return view.partition(name);
	}
}