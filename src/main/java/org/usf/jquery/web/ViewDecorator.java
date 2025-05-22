package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;

import java.util.Map;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public interface ViewDecorator {
	
	String identity(); //URL
	
	String columnName(ColumnDecorator cd); 
	
	default ViewBuilder builder() {
		return null; //no builder by default
	}

	default Builder<ViewDecorator, DBFilter> criteria(String name) { 
		return null; //no criteria by default
	}
	
	default Builder<ViewDecorator, Partition> partition(String name) {
		return null; //no partition by default
	}
	
	default Builder<ViewDecorator, ViewJoin[]> join(String name) {
		return null; //no join by default
	}
	
	default DBView view() {
		var env = currentEnvironment();
		return env.cacheView(identity(), 
				()-> env.getDatabase().view(this));
	}
	
	default NamedColumn column(@NonNull ColumnDecorator cd, String... args) {//final
		var name = columnName(cd);
		if(nonNull(name)) {
			return new ViewColumn(name, view(), cd.type(this), cd.reference(this));
		}
		var b = cd.builder();
		if(nonNull(b)) {
			return b.build(this, currentEnvironment(), args).as(cd.reference(this), cd.type(this));
		}
		throw undeclaredResouceException(cd.identity(), identity());
	}
	
	default ViewMetadata metadata(Map<String, ColumnMetadata> colMetadata) {
		return new ViewMetadata(this, colMetadata);
	}
}