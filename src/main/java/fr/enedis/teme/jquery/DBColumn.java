package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.CaseColumn.betweenIntervals;
import static fr.enedis.teme.jquery.CaseColumn.inValues;
import static fr.enedis.teme.jquery.OperatorExpression.equal;
import static fr.enedis.teme.jquery.OperatorExpression.greaterOrEquals;
import static fr.enedis.teme.jquery.OperatorExpression.greaterThan;
import static fr.enedis.teme.jquery.OperatorExpression.in;
import static fr.enedis.teme.jquery.OperatorExpression.isNotNull;
import static fr.enedis.teme.jquery.OperatorExpression.isNull;
import static fr.enedis.teme.jquery.OperatorExpression.lessOrEquals;
import static fr.enedis.teme.jquery.OperatorExpression.lessThan;
import static fr.enedis.teme.jquery.OperatorExpression.like;
import static fr.enedis.teme.jquery.OperatorExpression.notEquals;
import static fr.enedis.teme.jquery.OperatorExpression.notIn;
import static fr.enedis.teme.jquery.OperatorExpression.notLike;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import java.util.Map.Entry;

public interface DBColumn extends DBObject<DBTable>, Taggable<DBTable> {

	@Override
	default String sql(DBTable table, ParameterHolder arg) {
		return requireNonBlank(table.getColumnName(this));
	}

	@Override
	default String tag(DBTable table) {
		return requireNonBlank(table.getColumnName(this));
	}
	
	default boolean isExpression() {
		return false;
	}
	
	default boolean isAggregation() {
		return false;
	}
	
	default boolean isConstant() {
		return false;
	}

	default NamedColumn as(String name) {
		return new NamedColumn(name, this);
	}
	
	
	// filters	
    default ColumnFilter equalFilter(Object value){
    	return new ColumnFilter(this, equal(value));
    }
    
    default ColumnFilter notEqualFilter(Object value){
    	return new ColumnFilter(this, notEquals(value));
    }
    
    default ColumnFilter greaterThanExpression(Object min){
    	return new ColumnFilter(this, greaterThan(min));
    }

    default ColumnFilter greaterOrEqualFilter(Object min){
    	return new ColumnFilter(this, greaterOrEquals(min));
    }
    
    default ColumnFilter lessThanFilter(Object max){
    	return new ColumnFilter(this, lessThan(max));
    }
    
    default ColumnFilter lessOrEqualFilter(Object max){
    	return new ColumnFilter(this, lessOrEquals(max));
    }

	default ColumnFilter likeFilter(String value) {
		return new ColumnFilter(this, like(value));
	}

	default ColumnFilter notLikeFilter(String value) {
		return new ColumnFilter(this, notLike(value));
	}

    default <T> ColumnFilter inFilter(@SuppressWarnings("unchecked") T... values){
    	return new ColumnFilter(this, in(values));
    }
    
    default <T> ColumnFilter notInFilter(@SuppressWarnings("unchecked") T... values){
    	return new ColumnFilter(this, notIn(values));
    }

    default ColumnFilter nullFilter(){
    	return new ColumnFilter(this, isNull());
    }
    
    default ColumnFilter notNullFilter(){
    	return new ColumnFilter(this, isNotNull());
    }
    
    default <T> ColumnFilter filter(OperatorExpression<T> exp){
    	return new ColumnFilter(this, exp);
    }

	// case column
    default CaseColumn caseIntervals(Integer... values){
    	return betweenIntervals(this, values);
    }

    default CaseColumn caseIntervals(Double... values){
    	return betweenIntervals(this, values);
    }

    @SuppressWarnings("unchecked")
	default <T> CaseColumn caseValues(Entry<String, T[]>... values){
    	return inValues(this, values);
    }
    
}
