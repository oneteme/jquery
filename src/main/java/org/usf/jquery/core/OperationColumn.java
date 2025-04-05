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
	public int declare(RequestComposer composer, Consumer<DBColumn> groupKeys) {
		if(operator.is(AggregateFunction.class) || operator.is(WindowFunction.class)) {
			Nested.tryAggregation(composer, null, args);
			return 1;
		}
		if(operator.is("OVER")) {
			if(composer.getRole() == FILTER) {
				overColumn = new OperationColumn(operator, args, type).wrapView("over_" + hashCode());
				composer.ctes((QueryView) overColumn.getView());
				return overColumn.declare(composer, groupKeys);
			}
			return resolveOverColumns(composer, groupKeys);
		}
		return Nested.tryAggregation(composer, groupKeys, this, args);
	}
	
	private int resolveOverColumns(RequestComposer composer, Consumer<DBColumn> groupKeys) {
		requireAtLeastNArgs(1, args, ()-> "over"); //partition
		var lvl = Nested.tryAggregation(composer, groupKeys, args[0])-1; //nested aggregate function
		return args.length == 1
				? lvl
				: Math.max(lvl, Nested.tryAggregation(composer, groupKeys, args[1])); //partition
	}
		
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
