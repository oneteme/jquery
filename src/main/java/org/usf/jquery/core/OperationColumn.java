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
	private final JavaType type;

	public OperationColumn(Operator operation, Object[] args) {
		this(operation, args, null);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return operator.sql(builder, args);
	}
	
	@Override
	public JavaType getType() {
		return type;
	}
	
	@Override
	public boolean isAggregation() {
		return operator instanceof AggregateFunction 
				|| (!isOver() && Stream.of(args).anyMatch(Aggregable::aggregation)); //can do better
	}
	
	@Override
	public Stream<DBColumn> groupKeys() {
		if(isOver()) {
			return ((Partition)args[1]).groupKeys();
		}
		return operator instanceof AggregateFunction || operator instanceof ConstantOperator
				? Stream.empty() 
				: DBColumn.super.groupKeys();
	}
	
	private boolean isOver() { //specific operator
		return "OVER".equals(operator.id());
	}

	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
