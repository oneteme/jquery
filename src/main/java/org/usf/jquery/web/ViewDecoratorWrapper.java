package org.usf.jquery.web;

import org.usf.jquery.core.DBFilter;

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
	
	@Override
	public ViewBuilder builder() {
		return ()-> view.builder().build()::sql; //different reference
	}
	
	@Override
	public CriteriaBuilder<DBFilter> criteria(String name) {
		return view.criteria(name);
	}
	
	@Override
	public JoinBuilder join(String name) {
		return view.join(name);
	}
}