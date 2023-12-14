package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.AUTO;
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
	private Boolean aggregation;

	public OperationColumn(Operator operation, Object[] args) {
		this(operation, args, AUTO);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return operator.sql(builder, args);
	}

	@Override
	public JavaType javaType() {
		return type;
	}
	
	@Override
	public boolean isAggregation() {
		if(aggregation == null) {
			return operator.isAggregation() 
					|| Stream.of(args).anyMatch(NestedSql::aggregation);
		}
		return aggregation;
	}

	//see Operator::over
	OperationColumn aggregation(boolean aggregation) {
		this.aggregation = aggregation;
		return this;
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
