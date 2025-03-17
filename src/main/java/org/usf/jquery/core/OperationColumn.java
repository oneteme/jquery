package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.HashSet;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OperationColumn implements DBColumn {

	private final Operator operator;
	private final Object[] args; //optional
	private final JDBCType type; //optional
	private DBColumn overColumn;

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		if(nonNull(overColumn)) {
			overColumn.sql(sb, ctx); //no args
		}
		else {
			operator.sql(sb, ctx, args);
		}
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(overColumn) ? overColumn.getType() : type;
	}

	@Override
	public int columns(QueryBuilder builder, Consumer<DBColumn> groupKeys) {
		if(operator.is(AggregateFunction.class) || operator.is(WindowFunction.class)) {
			return Math.max(1, Nested.tryResolveColumn(builder, DO_NOTHING, args)+1); //if lvl==-1
		}
		if(operator.is("OVER")) {
			if(builder.getClause() == FILTER) {
				var views = new HashSet<DBView>();
				views(views::add);
				if(views.size() == 1) {
					var view = views.iterator().next();
					var cTag = "over_" + hashCode(); //over_view_hash
					builder.overView(view).getBuilder().columns(new OperationColumn(operator, args, type).as(cTag)); //clone
					overColumn = new ViewColumn(cTag, view, type, null);
					return overColumn.columns(builder, groupKeys);
				}
				throw new UnsupportedOperationException("over require only one view");
			}
			return resolveOverColumns(builder, groupKeys);
		}
		return Nested.tryResolveColumn(this, builder, groupKeys, args);
	}
	
	private int resolveOverColumns(QueryBuilder builder, Consumer<DBColumn> groupKeys) {
		requireAtLeastNArgs(1, args, ()-> "over");
		var lvl = Nested.tryResolveColumn(builder, groupKeys, args[0])-1; //nested aggregate function
		return args.length == 1
				? lvl
				: Math.max(lvl, Nested.tryResolveColumn(builder, groupKeys, args[1])); //partition
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		if(nonNull(overColumn)) {
			overColumn.views(cons);
		}
		else {
			Nested.tryResolveViews(cons, args);
		}
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
