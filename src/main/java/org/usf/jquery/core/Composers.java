package org.usf.jquery.core;

import static java.util.Arrays.copyOf;
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
import static org.usf.jquery.core.JQueryType.UNION;
import static org.usf.jquery.core.JQueryType.VIEW;
import static org.usf.jquery.core.JoinType.FULL;
import static org.usf.jquery.core.JoinType.INNER;
import static org.usf.jquery.core.JoinType.LEFT;
import static org.usf.jquery.core.JoinType.RIGHT;
import static org.usf.jquery.core.Parameter.optional;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.TypeResolver.firstArgType;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.function.IntFunction;

import org.usf.jquery.web.proxy.PartitionComposer;

/**
 * 
 * @author u$f
 *
 */
public interface Composers {

	//order operators
	
//	default Definition<Order> asc() {
//		return orderDefinition(ASC);
//	}
//	
//	default Definition<Order> desc() {
//		return orderDefinition(DESC);
//	}
//	
//	private Definition<Order> orderDefinition(OrderType type) {
//		return new Definition<>(type.name().toLowerCase(), ORDER, 
//				(t,args)-> ((Column)args[0]).order(type),
//				required(COLUMN));
//	}

	//case operators
	
	default Definition<CaseColumnComposer> when() { 
		return when(new CaseColumnComposer());
	}
	
	default Definition<CaseColumnComposer> when(CaseColumnComposer composer) { 
		return new Definition<>("when", CASE, 
				(type,args)-> composer.when((Criteria)args[0], args[1]), 
				required(FILTER), required());
	}
	
	default Definition<CaseColumn> orElse(CaseColumnComposer composer) { 
	    return new Definition<>("orElse", CASE, 
	    		(type,args) -> composer.orElse(args[0]), 
	    		required());
	}

	//partition operators
	
	default Definition<PartitionComposer> partition() { 
		return partition(new PartitionComposer());
	}
	
	default Definition<PartitionComposer> partition(PartitionComposer composer) { 
		return new Definition<>("partition", PARTITION, 
				(t, args)-> composer.columns((Column[])args), 
				varargs(COLUMN)); //can be empty for window functions without partition
	}
	
	default Definition<PartitionComposer> order(PartitionComposer composer) { //Partition | QueryComposer
		return new Definition<>("order", firstArgType(), 
				(type,args)-> composer.orders((Order[])args),
				required(ORDER), varargs(ORDER));
	}
	
	//join operators
	
	default Definition<JoinComposer> innerJoin() {
		return joinDefinition(INNER);
	}
	
	default Definition<JoinComposer> leftJoin() {
		return joinDefinition(LEFT);
	}

	default Definition<JoinComposer> rightJoin() {
		return joinDefinition(RIGHT);
	}
	
	default Definition<JoinComposer> fullJoin() {
		return joinDefinition(FULL);
	}
	
	private Definition<JoinComposer> joinDefinition(JoinType type) {
		return new Definition<>(type.name().toLowerCase()+"Join", JOIN, 
				(t,args)-> new JoinComposer(type, (DBView)args[0]), 
				required(VIEW));
	}
	
	default Definition<JoinComposer> criteria(JoinComposer composer) { //ViewJoin | QueryComposer
		return new Definition<>("criteria", firstArgType(), 
				(type,args)-> composer.criterias(convertArray(args, Criteria[].class)),
				required(FILTER), varargs(FILTER));
	}
	
	//query operators
	
	default Definition<QueryComposer> select() {
		return select(new QueryComposer());
	}
	
	default Definition<QueryComposer> select(QueryComposer composer) {
		return new Definition<>("select", QUERY, 
				(type,args)-> 
		composer.columns(convertArray(args, NamedColumn[].class)), 
				required(NAMED_COLUMN), varargs(NAMED_COLUMN));
	}
	
	default Definition<QueryComposer> criteria(QueryComposer composer) {
		return new Definition<>("criteria", QUERY, 
				(type,args)-> composer.filters(convertArray(args, Criteria[].class)), 
				required(FILTER), varargs(FILTER));
	}
	
	default Definition<QueryComposer> order(QueryComposer composer) {
		return new Definition<>("order", firstArgType(), 
				(type,args)-> composer.orders(convertArray(args, Order[].class)), 
				required(ORDER), varargs(ORDER));
	}
	
	default Definition<QueryComposer> join(QueryComposer composer){
		return new Definition<>("join", QUERY, 
				(t,args)-> composer.joins2(convertArray(args, JoinsClause[].class)), 
				required(JOIN), varargs(JOIN));
	}
	
	default Definition<QueryComposer> union(QueryComposer composer) {
		return new Definition<>("union", QUERY, 
				(t,args)-> composer.unions(convertArray(args, QueryUnion[].class)), 
				required(UNION), varargs(UNION));
	}
	
	default Definition<QueryComposer> distinct(QueryComposer composer) {
		return new Definition<>("distinct", QUERY, 
				(t,args)-> composer.distinct(isEmpty(args) || (Boolean) args[0]),
				optional(BOOLEAN));
	}
	
	default Definition<QueryComposer> limit(QueryComposer composer) {
		return queryIntDefinition("limit", composer::limit);
	}
	
	default Definition<QueryComposer> offset(QueryComposer composer) {
		return queryIntDefinition("offset", composer::offset);
	}
	
	private Definition<QueryComposer> queryIntDefinition(String name, IntFunction<QueryComposer> func) {
		return new Definition<>(name, QUERY, 
				(t,args)-> func.apply((Integer)args[0]),
				required(INTEGER)); //!variable
	}
	
	//TODO optimize args construction by creating right array type from signature and filling it directly instead of converting from Object[]
	static <T> T[] convertArray(Object[] array, Class<T[]> type) {
		return copyOf(array, array.length, type);
	}
}
