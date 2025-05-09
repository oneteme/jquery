package org.usf.jquery.web;

import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.ColumnMetadata.columnMetadata;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.TableView;
import org.usf.jquery.core.ViewColumn;

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
		return this::buildView;
	}

	default CriteriaBuilder<DBFilter> criteria(String name) { 
		return null; //no criteria by default
	}
	
	default JoinBuilder join(String name) {
		return null; //no join by default
	}
	
	default PartitionBuilder partition(String name) {
		return null; //no partition by default
	}
	
	default DBView view() {
		return metadata().getView();
	}
	
	default NamedColumn column(@NonNull ColumnDecorator cd) {//final
		var meta = metadata().columnMetadata(cd);
		if(nonNull(meta)) {
			return new ViewColumn(meta.getName(), view(), meta.getType(), cd.reference(this));
		}
		var b = cd.builder(this);
		if(nonNull(b)) {
			return b.build(this).as(cd.reference(this), cd.type(this));
		}
		throw undeclaredResouceException(cd.identity(), identity());
	}
	
	private TableView buildView() {
		var tn = currentContext().getDatabase().viewName(this);
		if(nonNull(tn)){
			var idx = tn.indexOf('.');
			return idx == -1 
					? new TableView(requireLegalVariable(tn), null, identity()) 
					: new TableView(
							requireLegalVariable(tn.substring(idx+1, tn.length())),
							requireLegalVariable(tn.substring(0, idx)), identity());
		}
		throw undeclaredResouceException(identity(), currentContext().getDatabase().identity());
	}

	default ViewMetadata metadata() {
		return currentContext().computeTableMetadata(this, cols-> 
			new ViewMetadata(requireNonNull(builder(), identity() + ".builder").build(), declaredColumns(this, cols)));
	}
	
	static Map<String, ColumnMetadata> declaredColumns(ViewDecorator vd, Collection<ColumnDecorator> cols){
		return cols.stream().<Entry<String,ColumnMetadata>>mapMulti((cd, acc)-> ofNullable(vd.columnName(cd))
						.map(cn-> entry(cd.identity(), columnMetadata(cn, cd.type(vd))))
						.ifPresent(acc)) //view column only
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}
	
	static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}	
}