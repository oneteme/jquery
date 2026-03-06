package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.TypeResolver.firstArgType;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.JQueryType.CASE;
import static org.usf.jquery.core.JQueryType.COLUMN;
import static org.usf.jquery.core.JQueryType.FILTER;
import static org.usf.jquery.core.JQueryType.JOIN;
import static org.usf.jquery.core.JQueryType.NAMED_COLUMN;
import static org.usf.jquery.core.JQueryType.ORDER;
import static org.usf.jquery.core.JQueryType.PARTITION;
import static org.usf.jquery.core.JQueryType.QUERY;
import static org.usf.jquery.core.JQueryType.VIEW;
import static org.usf.jquery.core.JoinType.FULL;
import static org.usf.jquery.core.JoinType.INNER;
import static org.usf.jquery.core.JoinType.LEFT;
import static org.usf.jquery.core.JoinType.RIGHT;
import static org.usf.jquery.core.OrderType.ASC;
import static org.usf.jquery.core.OrderType.DESC;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

import java.util.function.BiFunction;

/**
 * 
 * @author u$f
 *
 */
public interface Syntaxes {

	//factory methods
	
	default Definition<PartitionBuilder> partition() { 
		return new Definition<>(PARTITION, (t, args)-> {
			var builder = new PartitionBuilder();
			if(args.length > 0) {
				return builder.columns((Column[])args);
			}
			throw new IllegalArgumentException("cannot append field " + args[0]);
		}, varargs(COLUMN));
	}
	
	default Definition<QueryComposer> query() { 
		return new Definition<>(QUERY, (t,args)-> new QueryComposer());
	}

	default Definition<CaseColumnBuilder> choice() { 
		return new Definition<>(CASE, (t,args)-> new CaseColumnBuilder());
	}
	
	default Definition<ViewJoinBuilder> innerJoin() {
		return join(INNER);
	}
	
	default Definition<ViewJoinBuilder> leftJoin() {
		return join(LEFT);
	}

	default Definition<ViewJoinBuilder> rightJoin() {
		return join(RIGHT);
	}
	
	default Definition<ViewJoinBuilder> fullJoin() {
		return join(FULL);
	}
	
	private Definition<ViewJoinBuilder> join(JoinType type) {
		return new Definition<>(JOIN, (t,args)-> {
			if(args.length == 1 && args[0] instanceof DBView view) {
				return new ViewJoinBuilder(type, view);
			}
			throw new IllegalArgumentException("cannot append join " + args[0]);
		}, required(VIEW));
	}
	
	default Definition<CaseColumnBuilder> when() { 
		return new Definition<>(CASE, (t,args)-> {
			if(args.length == 3 
					&& args[0] instanceof CaseColumnBuilder part
					&& args[1] instanceof Criteria crt
					&& args[2] instanceof Column col) {
				return part.when(crt, col);
			}
			throw new IllegalArgumentException("cannot append when " + args[0]);
		}, required(CASE), required(FILTER), varargs(COLUMN));
	}
	
	default Definition<CaseColumnBuilder> otherwise() { 
	    return new Definition<>(CASE, (t,args) -> {
	        if(args.length == 2 && args[0] instanceof CaseColumnBuilder choice) {
	            return choice.orElse2(args[1]);
	        }
	        throw new IllegalStateException();
	    }, required(CASE), required());
	}
	
	default Definition<Composer<?>> field() { 
		return new Definition<>(QUERY, (t,args)-> {
			if(args.length > 1) {
				var fields = copyOfRange(args, 1, args.length, NamedColumn[].class);
				if(args[0] instanceof PartitionBuilder part) {
					return part.columns(fields);
				}
				if(args[0] instanceof QueryComposer query) {
					return query.columns(fields);
				}
			}
			throw new IllegalArgumentException("cannot append field " + args[0]);
		}, required(QUERY), required(NAMED_COLUMN), varargs(NAMED_COLUMN));
	}
	
	default Definition<Composer<?>> criteria() { 
		return new Definition<>("criteria", firstArgType(), (t,args)-> {
			if(args.length > 1) {
				var criteria = copyOfRange(args, 1, args.length, Criteria[].class);
				if(args[0] instanceof ViewJoinBuilder join) {
					return join.filters(criteria);
				}
				if(args[0] instanceof QueryComposer query) {
					return query.filters(criteria);
				}
			}
			throw new IllegalArgumentException("cannot append criteria " + args[0]);
		}, required(QUERY, JOIN), required(FILTER), varargs(FILTER));
	}
	
	default Definition<Composer<?>> order() { 
		return new Definition<>("order", firstArgType(), (t,args)-> {
			if(args.length > 1) {
				var order = copyOfRange(args, 1, args.length, Order[].class);
				if(args[0] instanceof PartitionBuilder part) {
					return part.orders(order);
				}
				if(args[0] instanceof QueryComposer query) {
					return query.orders(order);
				}
			}
			throw new IllegalStateException("cannot append order " + args[0]);
		}, required(QUERY, PARTITION), required(ORDER), varargs(ORDER));
	}
	
	default Definition<Order> asc() {
		return orderBy(ASC);
	}
	
	default Definition<Order> desc() {
		return orderBy(DESC);
	}
	
	private Definition<Order> orderBy(OrderType type) {
		return new Definition<>(ORDER, (t,args)-> {
			if(args.length > 1 && args[0] instanceof Column col) {
				return col.order(type);
			}
			throw new IllegalStateException("cannot append order by " + args[0]);
		}, required(COLUMN));
	}
	
	default Definition<QueryComposer> limit() {
		return queryIntFunction("limit", QueryComposer::limit);
	}
	
	default Definition<QueryComposer> offset() {
		return queryIntFunction("offset", QueryComposer::offset);
	}
	
	private Definition<QueryComposer> queryIntFunction(String name, BiFunction<QueryComposer, Integer, QueryComposer> func) {
		return new Definition<>(QUERY, (t,args)-> {
			if(args.length == 2 && args[0] instanceof QueryComposer query && args[1] instanceof Integer v) {
				return func.apply(query, v);
			}
			throw new IllegalStateException("cannot append " + name + " " + args[0]);
		}, required(QUERY), required(INTEGER));
	}
	
}
