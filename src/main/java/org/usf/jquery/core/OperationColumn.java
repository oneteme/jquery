package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.Validation.requireNArgs;

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
	public boolean resolve(QueryBuilder builder, Consumer<? super DBColumn> groupKeys) {
		if(operator.is(AggregateFunction.class)) {
			builder.setAggregation(true);
			return true; //aggregate function calls cannot be nested
		}
		if(operator.is("OVER")) { //TD : nested aggregation avg(count(*)) over(..)
			if(builder.getClause() == FILTER) {
				var views = new HashSet<DBView>();
				views(views::add);
				if(views.size() == 1) {
					var view = views.iterator().next();
					var cTag = "over_" + hashCode(); //over_view_hash
					builder.overView(view).getBuilder().columns(new OperationColumn(operator, args, type).as(cTag)); //clone
					overColumn = new ViewColumn(cTag, view, type, null);
					return overColumn.resolve(builder, groupKeys);
				}
				throw new UnsupportedOperationException("over require only one view");
			}
			return requirePartition().resolve(builder, groupKeys); //!grouping keys 
		}
		return operator.is(ConstantOperator.class) || Nested.tryResolve(builder, groupKeys, args);
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		if(nonNull(overColumn)) {
			overColumn.views(cons);
		}
		else {
			Nested.viewsOf(cons, args);
		}
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
	
	private Partition requirePartition() {
		if(requireNArgs(2, args, ()-> "over operation")[1] instanceof Partition part) {
			return part;
		}
		throw new IllegalArgumentException("partition parameter expected");
	}
}
