package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.Nested.viewsOfAll;
import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.Validation.requireNArgs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

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
	public String sql(QueryVariables qv) {
		return Objects.isNull(overColumn) ? operator.sql(qv, args) : overColumn.sql(qv);
	}
	
	@Override
	public JDBCType getType() {
		return Objects.isNull(overColumn) ? type : overColumn.getType();
	}

	@Override
	public boolean resolve(QueryBuilder builder) {
		if(operator.is(AggregateFunction.class)) {
			builder.aggregation();
			return true;
		}
		else if(isOverFunction()) {
			if(builder.getClause() == FILTER) {
				var views = new HashSet<DBView>();
				views(views);
				if(views.size() == 1) {
					var view = views.iterator().next();
					var cTag = "over_" + hashCode(); //over_view_hash
					builder.overView(view).getBuilder().columns(new OperationColumn(operator, args, type).as(cTag)); //clone
					this.overColumn = new ViewColumn(cTag, view, getType(), null);
					return false;
				}
				throw new UnsupportedOperationException("require only one view");
			}
			return requirePartition().resolve(builder); //no aggregation
		}
		return operator.is(ConstantOperator.class) 
				|| Nested.tryResolveAll(builder, args);
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
	
	boolean isOverFunction() {
		return "OVER".equals(operator.id());
	}
	
	private Partition requirePartition() {
		if(requireNArgs(2, args, ()-> "over operation")[1] instanceof Partition part) {
			return part;
		}
		throw new IllegalArgumentException("partition parameter expected");
	}
}
