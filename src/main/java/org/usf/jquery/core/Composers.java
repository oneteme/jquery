package org.usf.jquery.core;

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
import static org.usf.jquery.core.Signature.arrayOf;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.function.IntFunction;

import org.usf.jquery.web.proxy.PartitionComposer;

/**
 * 
 * @author u$f
 *
 */
public interface Composers {

	//case operators
	
	default ComposerDefinition<CaseColumnComposer> when() { 
		return when(new CaseColumnComposer());
	}
	
	default ComposerDefinition<CaseColumnComposer> when(CaseColumnComposer composer) { 
		return new ComposerDefinition<>("when", CASE, 
				args-> composer.when((Criteria)args[0], args[1]), 
				required(FILTER), required());
	}
	
	default ComposerDefinition<CaseColumn> orElse(CaseColumnComposer composer) { 
	    return new ComposerDefinition<>("orElse", CASE, 
	    		args -> composer.orElse(args[0]), 
	    		required());
	}

	//partition operators
	
	default ComposerDefinition<PartitionComposer> partition() { 
		return partition(new PartitionComposer());
	}
	
	default ComposerDefinition<PartitionComposer> partition(PartitionComposer composer) { 
		return new ComposerDefinition<>("partition", PARTITION, 
				args-> composer.columns((Column[])args), 
				arrayOf(COLUMN)); //can be empty for window functions without partition
	}
	
	default ComposerDefinition<PartitionComposer> order(PartitionComposer composer) { //Partition | QueryComposer
		return new ComposerDefinition<>("order", PARTITION, 
				args-> composer.orders((Order[])args),
				arrayOf(ORDER, 1));
	}
	
	//join operators
	
	default ComposerDefinition<JoinComposer> innerJoin() {
		return joinDefinition(INNER);
	}
	
	default ComposerDefinition<JoinComposer> leftJoin() {
		return joinDefinition(LEFT);
	}

	default ComposerDefinition<JoinComposer> rightJoin() {
		return joinDefinition(RIGHT);
	}
	
	default ComposerDefinition<JoinComposer> fullJoin() {
		return joinDefinition(FULL);
	}
	
	private ComposerDefinition<JoinComposer> joinDefinition(JoinType type) {
		return new ComposerDefinition<>(type.name().toLowerCase()+"Join", JOIN, 
				args-> new JoinComposer(type, (DBView)args[0]), 
				required(VIEW));
	}
	
	default ComposerDefinition<JoinComposer> criteria(JoinComposer composer) { //ViewJoin | QueryComposer
		return new ComposerDefinition<>("criteria", JOIN, 
				args-> composer.criterias((Criteria[]) args),
				arrayOf(FILTER, 1));
	}
	
	//query operators
	
	default ComposerDefinition<QueryComposer> select() {
		return select(new QueryComposer());
	}
	
	default ComposerDefinition<QueryComposer> select(QueryComposer composer) {
		return new ComposerDefinition<>("select", QUERY, 
				args-> composer.columns((NamedColumn[]) args), 
				arrayOf(NAMED_COLUMN, 1));
	}
	
	default ComposerDefinition<QueryComposer> join(QueryComposer composer){
		return new ComposerDefinition<>("join", QUERY, 
				args-> composer.joins2((JoinsClause[]) args), 
				arrayOf(JOIN, 1));
	}
	
	default ComposerDefinition<QueryComposer> criteria(QueryComposer composer) {
		return new ComposerDefinition<>("criteria", QUERY, 
				args-> 
		composer.criterias((Criteria[]) args), 
				arrayOf(FILTER, 1));
	}
	
	default ComposerDefinition<QueryComposer> group(QueryComposer composer) {
		return new ComposerDefinition<>("group", QUERY, 
				args-> 
		composer.groups((Column[]) args), 
				arrayOf(COLUMN, 1));
	}
	
	default ComposerDefinition<QueryComposer> order(QueryComposer composer) {
		return new ComposerDefinition<>("order", QUERY, 
				args-> composer.orders((Order[]) args), 
				arrayOf(ORDER, 1));
	}
	
	default ComposerDefinition<QueryComposer> union(QueryComposer composer) {
		return new ComposerDefinition<>("union", QUERY, 
				args-> composer.unions((QueryUnion[]) args), 
				arrayOf(UNION, 1));
	}
	
	default ComposerDefinition<QueryComposer> distinct(QueryComposer composer) {
		return new ComposerDefinition<>("distinct", QUERY, 
				args-> composer.distinct(isEmpty(args) || (Boolean) args[0]),
				optional(BOOLEAN));
	}
	
	default ComposerDefinition<QueryComposer> limit(QueryComposer composer) {
		return queryIntDefinition("limit", composer::limit);
	}
	
	default ComposerDefinition<QueryComposer> offset(QueryComposer composer) {
		return queryIntDefinition("offset", composer::offset);
	}
	
	private ComposerDefinition<QueryComposer> queryIntDefinition(String name, IntFunction<QueryComposer> func) {
		return new ComposerDefinition<>(name, QUERY, 
				args-> func.apply((Integer)args[0]),
				required(INTEGER)); //!variable
	}
}
