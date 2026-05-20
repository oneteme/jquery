package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.View;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.Join;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class ViewDecoratorWrapper implements ViewDecorator {

	//do no use @Delegate
	private final ViewDecorator vd;
	private final String id;
	private View viewRef;
		
	@Override
	public String identity() {
		return id;
	}

	@Override
	public String columnName(ColumnDecorator cd) {
		return vd.columnName(cd);
	}
	
	@Override
	public View view() {
		if(isNull(viewRef)) { // do not use env.cache
			viewRef = currentEnvironment().getDatabase().view(vd); 
		}
		return viewRef;
	}
	
	@Override
	public Builder<ViewDecorator, Criteria> criteriaBuilder(String name) {
		return vd.criteriaBuilder(name);
	}
	
	@Override
	public Builder<ViewDecorator, Join[]> joinBuilder(String name) {
		return vd.joinBuilder(name);
	}
	
	@Override
	public Builder<ViewDecorator, Partition> partitionBuilder(String name) {
		return vd.partitionBuilder(name);
	}
}