package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
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
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.OrderType.ASC;
import static org.usf.jquery.core.OrderType.DESC;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.TypeResolver.firstArgType;

import java.util.function.BiFunction;

/**
 * 
 * @author u$f
 *
 */
public interface Syntaxes {
	
	//join operators
	
	default Definition<ViewJoin> innerJoin() {
		return join(INNER);
	}
	
	default Definition<ViewJoin> leftJoin() {
		return join(LEFT);
	}

	default Definition<ViewJoin> rightJoin() {
		return join(RIGHT);
	}
	
	default Definition<ViewJoin> fullJoin() {
		return join(FULL);
	}
	
	private Definition<ViewJoin> join(JoinType type) {
		return new Definition<>(type.name().toLowerCase()+"Join", JOIN, 
				(t,args)-> new ViewJoin(type, (DBView)args[0], null), 
				required(VIEW));
	}

	//partition operators
	
	default Definition<Partition> partition() { 
		return new Definition<>("partition", PARTITION, 
				(t, args)-> new Partition((Column[])args, null), 
				varargs(COLUMN)); //can be empty for window functions without partition
	}
	
	//query operators
	
	default Definition<QueryComposer> get() {
		return new Definition<>("get", QUERY, 
				(type,args)-> new QueryComposer().columns(copyOfRange(args, 1, args.length, NamedColumn[].class)), 
				required(NAMED_COLUMN), varargs(NAMED_COLUMN));
	}
	
	default Definition<QueryComposer> limit() {
		return queryIntFunction("limit", QueryComposer::limit);
	}
	
	default Definition<QueryComposer> offset() {
		return queryIntFunction("offset", QueryComposer::offset);
	}
	
	private Definition<QueryComposer> queryIntFunction(String name, BiFunction<QueryComposer, Integer, QueryComposer> func) {
		return new Definition<>(name, QUERY, 
				(t,args)-> func.apply((QueryComposer)args[0], (Integer)args[1]),
				required(QUERY), required(INTEGER));
	}
	
	//case operators
	
	default Definition<CaseColumn> choice() { 
		return new Definition<>("choice", CASE, 
				(type,args)-> new CaseColumn());
	}
	
	default Definition<CaseColumn> when() { 
		return new Definition<>("when", CASE, 
				(type,args)-> ((CaseColumn)args[0]).when(new WhenCase((Criteria)args[1], args[2])), 
				required(CASE), required());
	}
	
	default Definition<CaseColumn> orElse() { 
	    return new Definition<>("orElse", CASE, 
	    		(type,args) -> ((CaseColumn)args[0]).when(new WhenCase(null, args[1])), 
	    		required(CASE), required());
	}

	//order operators
	
	default Definition<Order> asc() {
		return orderBy(ASC);
	}
	
	default Definition<Order> desc() {
		return orderBy(DESC);
	}
	
	private Definition<Order> orderBy(OrderType type) {
		return new Definition<>(type.name().toLowerCase(), ORDER, 
				(t,args)-> ((Column)args[0]).order(type),
				required(COLUMN));
	}

	//logical operators
	
	default Definition<Criteria> and() {
		return chain(AND);
	}

	default Definition<Criteria> or() {
		return chain(OR);
	}
	
	private Definition<Criteria> chain(LogicalOperator opr) {
		return new Definition<>(opr.name().toLowerCase(), BOOLEAN, 
				(type,args)-> ((Criteria)args[0]).append(opr, (Criteria)args[1]), 
				required(BOOLEAN), required(BOOLEAN));
	}
	
	//scope operators
	
	default Definition<Column> over() {
		return new OperatorDefinition(firstArgType(), scope("OVER"), required(), optional(PARTITION)); 
	}
	
	default Definition<Column> within() {
		return new OperatorDefinition(firstArgType(), scope("WITHIN GROUP"), required(), varargs(ORDER));
	}
	
	//common operators for query and join
	
	default Definition<Object> criteria() {
		return new Definition<>("criteria", firstArgType(), (type,args)-> {
			var criteria = copyOfRange(args, 1, args.length, Criteria[].class);
			if(args[0] instanceof ViewJoin join) {
				return join.criterias(criteria);
			}
			if(args[0] instanceof QueryComposer query) {
				return query.filters(criteria);
			}
			throw new IllegalStateException("unexpected argument '" + args[0] + "' for criteria operator");
		}, required(QUERY, JOIN), required(FILTER), varargs(FILTER));
	}
	
	default Definition<Object> order() {
		return new Definition<>("order", firstArgType(), (type,args)-> {
			var order = copyOfRange(args, 1, args.length, Order[].class);
			if(args[0] instanceof Partition part) {
				return part.orders(order);
			}
			if(args[0] instanceof QueryComposer query) {
				return query.orders(order);
			}
			throw new IllegalStateException("unexpected argument '" + args[0] + "' for order operator");
		}, required(QUERY, PARTITION), required(ORDER), varargs(ORDER));
	}

	public static ScopeFunction scope(String name) {
		return ()-> name;
	}
}
