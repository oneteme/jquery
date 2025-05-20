package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
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
	private final ViewDecorator vd;
	private final String id;
	private DBView viewRef;
		
	@Override
	public String identity() {
		return id;
	}

	@Override
	public String columnName(ColumnDecorator cd) {
		return vd.columnName(cd);
	}
	
	@Override
	public DBView view() {
		if(isNull(viewRef)) { // do not cache viewRef
			var env = currentEnvironment();
			viewRef = env.getDatabase().view(vd); 
		}
		return viewRef;
	}
	
	@Override
	public Builder<DBFilter> criteria(String name) {
		return vd.criteria(name);
	}
	
	@Override
	public Builder<ViewJoin[]> join(String name) {
		return vd.join(name);
	}
	
	@Override
	public Builder<Partition> partition(String name) {
		return vd.partition(name);
	}
}