package org.usf.jquery.core;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.OperatorKind.AGGREGATE;
import static org.usf.jquery.core.OperatorKind.WINDOW;
import static org.usf.jquery.core.QueryManifest.Section.CRITERIA;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.List;

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
	private final List<Object> args; //optional
	private final JDBCType type; //optional
	private ViewColumn overColumn;

	@Override
	public int prepare(QueryManifest manifest) {
		if(kind == AGGREGATE || kind == WINDOW) {
			manifest.ignoreGroups(d-> d.tryPrepareNested(args)); //declare views only
			return MEASURE;
		}
		if("OVER".equals(name)) {
			if(manifest.getRole() == CRITERIA) {
				var col = new OperationColumn(name, kind, operator, args, type).as(name + '_' + hashCode());
				var sub = new QueryComposer().columns(Column.allColumns(), col).compose(manifest.getStore());
				this.overColumn = sub.column(col.getTag(), col.getType());
				manifest.cte(sub, true);
				return overColumn.prepare(manifest);
			}
			return resolveOverColumns(manifest);
		}
		return manifest.tryPrepareNestedOrElse(args, this);
	}

	@Override
	public void build(QueryBuilder builder) {
		if(nonNull(overColumn)) {
			builder.append(overColumn); //no args
		}
		else {
			operator.build(builder, args.toArray());
		}
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(overColumn) ? overColumn.getType() : type;
	}
	
	private int resolveOverColumns(QueryManifest declare) {
		requireAtLeastNArgs(1, args, ()-> "over"); //partition
		var lvl = declare.tryPrepareNested(asList(args.get(0)))-1; //nested aggregate function
		return args.size() == 1
				? lvl
				: Math.max(lvl, declare.tryPrepareNested(asList(args.get(1)))); //partition
	}
		
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
