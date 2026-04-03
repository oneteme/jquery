package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Stream.empty;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public class QueryDeclaration {

	static final Consumer<Column> DECLARE_ONLY = o->{};

	private final Set<DBView> views; //views
	private final Consumer<Column> groups;
	
	private Role role;
	
	public QueryDeclaration declare(DBView view) {
		views.add(view);
		return this;
	}
	
	public QueryDeclaration groupBy(Column column) {
		groups.accept(column);
		return this;
	}
	
	public QueryDeclaration cte(DBView view) {
		//TODO 
		return this;
	}
	
	public int composeNested(DBObject... args){
		return composeNestedOrElse(args, null);
	}
	
	public int composeNestedOrElse(DBObject[] args, Column col){
		return composeNested(streamOrEmpty(args), col);
	}

	public int tryComposeNested(Object... args){
		return tryComposeNestedOrElse(args, null);
	}

	public int tryComposeNestedOrElse(Object[] args, Column col){
		return composeNested(streamOrEmpty(args).mapMulti((o, acc)->{
			if(o instanceof DBObject n) {
				acc.accept(n);
			}
		}), col);
	}

	int composeNested(Stream<DBObject> stream, Column orElse){
		if(groups == DECLARE_ONLY) {
			stream.forEach(o-> o.compose(this)); //declare views only, no group keys
			return -1;
		}
		if(isNull(orElse)) {
			return stream.mapToInt(o-> 
			o.compose(this))
					.max().orElse(-1);
		}
		var arr = new ArrayList<Column>();
		var sub = sub(arr::add);
		var lvl = stream.mapToInt(o-> o.compose(sub)).max().orElse(-1);
		if(lvl == 0) { //group keys
			groups.accept(orElse);
		}
		else if(lvl > 0 && !arr.isEmpty()) {
			arr.forEach(groups);
		}
		return lvl;
	}
	
	public QueryDeclaration sub(Consumer<Column> group) {
		return new QueryDeclaration(views, group);
	}

	private static <T> Stream<T> streamOrEmpty(T[] arr) {
		return isEmpty(arr) ? empty() : stream(arr);
	}
}
