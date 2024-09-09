package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.Nested.tryResolveAll;
import static org.usf.jquery.core.Nested.viewsOfAll;
import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.Validation.requireNArgs;

import java.util.Collection;
import java.util.HashSet;

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

	public OperationColumn(Operator operation, Object[] args) {
		this(operation, args, null);
	}
	
	@Override
	public String sql(QueryVariables vars) {
		return nonNull(overColumn) ? overColumn.sql(vars) : operator.sql(vars, args);
	}
	
	@Override
	public JDBCType getType() {
		return nonNull(overColumn) ? overColumn.getType() : type;
	}

	@Override
	public boolean resolve(QueryBuilder builder) {
		if(operator.is(AggregateFunction.class)) {
			builder.aggregation();
			return true;
		}
		else if(operator.is("OVER")) {
			if(builder.getClause() == FILTER) {
				var views = new HashSet<DBView>();
				views(views);
				if(views.size() == 1) {
					var view = views.iterator().next();
					var cTag = "over_" + hashCode(); //over_view_hash
					builder.overView(view).getBuilder().columns(new OperationColumn(operator, args, type).as(cTag)); //clone
					overColumn = new ViewColumn(cTag, view, type, null);
					return false;
				}
				throw new UnsupportedOperationException("require only one view");
			}
			return requirePartition().resolve(builder); //no aggregation
		}
		return operator.is(ConstantOperator.class) 
				|| tryResolveAll(builder, args);
	}
	
	@Override
	public void views(Collection<DBView> views) {
		if(nonNull(overColumn)) {
			overColumn.views(views);
		}
		else {
			viewsOfAll(views, args);
		}
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
	private Partition requirePartition() {
		if(requireNArgs(2, args, ()-> "over operation")[1] instanceof Partition part) {
			return part;
		}
		throw new IllegalArgumentException("partition parameter expected");
	}
}
