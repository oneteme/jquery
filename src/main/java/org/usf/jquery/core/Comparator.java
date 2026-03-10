package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.JDBCType.BOOLEAN;

import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
public interface Comparator extends Processor<Criteria> {
	
	String id();
	
	@Override
	default int compose(QueryComposer composer, Consumer<Column> groupKeys) {
		throw new UnsupportedOperationException("compose comparator");
	}

	@Override
	default SimpleCriteria invoke(JavaType type, Object... args) {
		if(type == BOOLEAN) {
			return new SimpleCriteria(args[0], 
					expression(copyOfRange(args, 1, args.length))); // no type
		}
		throw new IllegalArgumentException("operator " + id() + " cannot be applied to type " + type);
	}

	default SimplePredicate expression(Object... right) {
		return new SimplePredicate(this, right, null);
	}
	
	@Deprecated
	default SimplePredicate expression(Adjuster<Object[]> adj, Object... initalValue) {
		return new SimplePredicate(this, initalValue, adj);
	}
	
	default boolean is(Class<? extends Comparator> type) { 
		return type.isInstance(this);
	}
}
