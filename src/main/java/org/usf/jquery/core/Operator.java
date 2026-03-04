package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
public interface Operator extends DBProcessor {
	
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

	default Column operation(JDBCType type, Object... args) {
		return new OperationColumn(this, args, type);
	}
	
	default boolean is(Class<? extends Operator> type) {
		return type.isInstance(this);
	}
	
	default boolean is(String name) {
		return name.equals(id());
	}
}
