package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OperationColumn implements DBColumn {

	@NonNull
	private final DBOperation operation;
	private final Object[] args; //

	@Override
	public String sql(QueryParameterBuilder builder) {
		return operation.sql(builder, args);
	}

	@Override
	public boolean isAggregation() {
		return operation.isAggregation() 
				|| Stream.of(args).anyMatch(NestedSql::aggregation);
	}

	@Override
	public boolean isConstant() {
		return Stream.of(args).allMatch(DBColumn::isColumnConstant);
	}

	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
