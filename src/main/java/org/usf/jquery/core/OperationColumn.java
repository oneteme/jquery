package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.stream.Stream;

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
	private final Object[] args;
	private final JDBCType type;

	public OperationColumn(Operator operation, Object[] args) {
		this(operation, args, null);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return operator.sql(builder, args);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}
	
	@Override
	public boolean isAggregation() {
		return operator instanceof AggregateFunction || 
				(!isOverFunction() && Stream.of(args).anyMatch(Nested::aggregation)); //can do better
	}
	
	@Override
	public Stream<DBColumn> groupKeys() {
		if(isOverFunction()) {
			return ((Partition)args[1]).groupKeys();
		}
		return operator instanceof AggregateFunction || operator instanceof ConstantOperator
				? Stream.empty() 
				: DBColumn.super.groupKeys();
	}
	
	boolean isOverFunction() {
		return "OVER".equals(operator.id());
	}

	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
}
