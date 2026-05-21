package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;

import java.util.Map;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.Join;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.View;

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

	default Builder<ViewDecorator, Criteria> criteriaBuilder(String name) { 
		return null; //no criteria by default
	}
	
	default Builder<ViewDecorator, Join[]> joinBuilder(String name) {
		return null; //no join by default
	}
	
	default Builder<ViewDecorator, Partition> partitionBuilder(String name) {
		return null; //no partition by default
	}
	
	default View view() { //takes no args : single instance !?
		var env = currentEnvironment();
		return env.cacheView(identity(), ()-> env.getDatabase().view(this));
	}

//	default NamedColumn column(@NonNull String id, String... args) //final
	
	default Column column(@NonNull ColumnDecorator cd, String... args) {//final
		var name = columnName(cd);
		if(nonNull(name)) {
			return view().column(name, cd.type(this), cd.reference(this));
		}
		var b = cd.builder();
		if(nonNull(b)) {
			return b.build(this, args).as(cd.reference(this), cd.type(this));
		}
		throw noSuchResourceException(COLUMN_PARAM, cd.identity(), identity());
	}
	
	default Criteria criteria(String name, String...args) {
		return requireNonNull(criteriaBuilder(name), "criteriaBuilder")
				.build(this, args);
	}

	default Join[] join(String name, String...args) {
		return requireNonNull(joinBuilder(name), "joinBuilder")
				.build(this, args);
	}
	
	default Partition partition(String name, String...args) {
		return requireNonNull(partitionBuilder(name), "partitionBuilder")
				.build(this, args);
	}
	
	default ViewMetadata metadata(Map<String, ColumnMetadata> colMetadata) {
		return new ViewMetadata(this, colMetadata);
	}

	default ViewMetadata metadata() {
		return currentEnvironment().getMetadata().viewMetadata(this);
	}
}