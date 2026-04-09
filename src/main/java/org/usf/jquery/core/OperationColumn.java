package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.OperatorKind.AGGREGATE;
import static org.usf.jquery.core.OperatorKind.WINDOW;
import static org.usf.jquery.core.QueryDeclaration.DECLARE_VIEW_ONLY;
import static org.usf.jquery.core.Role.CRITERIA;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

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
	public int compose(QueryDeclaration declare) {
		if(kind == AGGREGATE || kind == WINDOW) {
			declare.sub(DECLARE_VIEW_ONLY).tryComposeNested(args); //declare views only
			return 1;
		}
		if("OVER".equals(name)) {
			if(declare.getRole() == CRITERIA) {
				var col = new OperationColumn(name, kind, operator, args, type).as(name + '_' + hashCode());
				var sub = new SubView(new QueryComposer().columns(Column.allColumns(), col).compose());
				this.overColumn = sub.column(col.getTag(), col.getType());
				return overColumn.compose(declare);
			}
			return resolveOverColumns(declare);
		}
		return declare.tryComposeNestedOrElse(args, this);
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
	
	private int resolveOverColumns(QueryDeclaration declare) {
		requireAtLeastNArgs(1, args, ()-> "over"); //partition
		var lvl = declare.tryComposeNested(args[0])-1; //nested aggregate function
		return args.length == 1
				? lvl
				: Math.max(lvl, declare.tryComposeNested(args[1])); //partition
	}
		
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
