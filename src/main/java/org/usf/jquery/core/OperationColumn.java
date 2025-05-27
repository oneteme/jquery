package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Role.FILTER;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OperationColumn implements DBColumn {

	private final Operator operator;
	private final Object[] args; //optional
	private final JDBCType type; //optional
	private ViewColumn overColumn; 

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		if(operator.is(AggregateFunction.class) || operator.is(WindowFunction.class)) {
			DBObject.tryComposeNested(query, c-> {}, args); //declare views only
			return 1;
		}
		if(operator.is("OVER")) {
			if(query.getRole() == FILTER) {
				overColumn = replaceNestedView(query);
				return overColumn.compose(query, groupKeys);
			}
			return resolveOverColumns(query, groupKeys);
		}
		return DBObject.tryComposeNested(query, groupKeys, this, args);
	}

	@Override
	public void build(QueryBuilder query) {
		if(nonNull(overColumn)) {
			query.append(overColumn); //no args
		}
		else {
			operator.build(query, args);
		}
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(overColumn) ? overColumn.getType() : type;
	}
	
	private int resolveOverColumns(QueryComposer composer, Consumer<DBColumn> groupKeys) {
		requireAtLeastNArgs(1, args, ()-> "over"); //partition
		var lvl = DBObject.tryComposeNested(composer, groupKeys, args[0])-1; //nested aggregate function
		return args.length == 1
				? lvl
				: Math.max(lvl, DBObject.tryComposeNested(composer, groupKeys, args[1])); //partition
	}
		
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	ViewColumn replaceNestedView(QueryComposer query) {
		var col = new OperationColumn(operator, args, type).as("over_" + hashCode());
		var views = new QueryComposer().columns(col).getViews(); //scan column views
		if(views.size() == 1) {
			var sub = query.subQuery(views.iterator().next()); //get existing subQuery
			sub.getComposer().columns(col); 
			return new ViewColumn(col.getTag(), sub, col.getType(), null);
		}
		throw new UnsupportedOperationException("overview require only one view");
	}
}
