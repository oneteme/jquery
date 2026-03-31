package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.OperatorKind.AGGREGATE;
import static org.usf.jquery.core.OperatorKind.WINDOW;
import static org.usf.jquery.core.Role.CRITERIA;
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
public final class OperationColumn implements Column {

	private final String name;
	private final OperatorKind kind;
	private final Invocable operator;
	private final Object[] args; //optional
	private final JDBCType type; //optional
	private ViewColumn overColumn; 

	@Override
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		if(kind == AGGREGATE || kind == WINDOW) {
			DBObject.tryComposeNested(query, DECLARE_ONLY, args); //declare views only
			return 1;
		}
		if(name.equals("OVER")) {
			if(query.getRole() == CRITERIA) {
				var col = new OperationColumn(name, kind, operator, args, type).as("over_" + hashCode());
				var sub = new SubView(new QueryComposer().columns(Column.allColumns(), col).compose2());
				this.overColumn = sub.column(col.getTag(), col.getType());
				return overColumn.compose(query, groupKeys);
			}
			return resolveOverColumns(query, groupKeys);
		}
		return DBObject.tryComposeNestedOrElse(query, groupKeys, args, this);
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
	
	private int resolveOverColumns(QueryComposer composer, Consumer<Column> groupKeys) {
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
}
