package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
public interface Operator extends Processor<Column> {
	
	String id(); //nullable

	void buildOperator(QueryBuilder builder, Object... args);

	@Override
	default int compose(QueryComposer composer, Consumer<Column> groupKeys) {
		throw new UnsupportedOperationException("compose operator");
	}
	
	@Override
	default void build(QueryBuilder builder, Object... args) {
		var op = this;
		var prd = builder.getEnvironment().getProduct();
		if(nonNull(prd)) {
			op = prd.replace(op);
		}
		op.buildOperator(builder, args);
	}

	@Override
	default Column invoke(JavaType type, Object... args) {
		if(isNull(type) || type instanceof JDBCType) {
			return new OperationColumn(this, args, (JDBCType)type);
		}
		throw new IllegalArgumentException("operator " + id() + " cannot be applied to type " + type);
	}
	
	default boolean is(Class<? extends Operator> type) {
		return type.isInstance(this);
	}
	
	default boolean is(String name) {
		return name.equals(id());
	}
}
