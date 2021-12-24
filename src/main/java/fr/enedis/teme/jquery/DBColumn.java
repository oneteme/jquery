package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.IntervalCaseExpression.intervals;
import static fr.enedis.teme.jquery.ValuesCaseExpression.values;

import lombok.NonNull;

public interface DBColumn extends DBObject<DBTable> {

	String getMappedName();

	default String toSql(@NonNull DBTable table) {
		return table.getColumnName(this);
	}
	
	default String sqlAlias(@NonNull DBTable table) {
		return table.getColumnName(this);
	}
	
	default boolean isAggregated() {
		return false;
	}
	
	default boolean isExpression() {
		return false;
	}
	
	default boolean isConstant() {
		return false;
	}

	// filters
    default NullFilter isNull(){
    	return new NullFilter(this, false);
    }
    
    default NullFilter isNotNull(){
    	return new NullFilter(this, true);
    }
	
    default <T> InFilter<T> equal(T value){
    	return new InFilter<>(this, false, value);
    }
    
    default <T> InFilter<T> notEqual(T value){
    	return new InFilter<>(this, true, value);
    }

    default <T> InFilter<T> in(@SuppressWarnings("unchecked") T... values){
    	return new InFilter<>(this, false, values);
    }
    
    default <T> InFilter<T> notIn(@SuppressWarnings("unchecked") T... values){
    	return new InFilter<>(this, true, values);
    }

    default <T> IntervalFilter<T> between(T min, T max){
    	return new IntervalFilter<>(this, min, true, max, true);
    }

    default <T> IntervalFilter<T> interval(T min, boolean orMinEquals, T max, boolean orMaxEquals){
    	return new IntervalFilter<>(this, min, orMinEquals, max, orMaxEquals);
    }
    
    default <T> IntervalFilter<T> greaterThan(T min){
    	return new IntervalFilter<>(this, min, null);
    }

    default <T> IntervalFilter<T> greaterThanOrEquals(T min){
    	return new IntervalFilter<>(this, min, true, null, false);
    }
    
    default <T> IntervalFilter<T> lessThan(T max){
    	return new IntervalFilter<>(this, null, max);
    }
    
    default <T> IntervalFilter<T> lessThanOrEquals(T max){
    	return new IntervalFilter<>(this, null, false, max, true);
    }

	// function columns
    
    default FunctionColumn caseIntervals(int... value){
    	return new FunctionColumn(intervals(value), this, null);
    }

    default FunctionColumn caseIntervals(String mappedName, int... value){
    	return new FunctionColumn(intervals(value), this, mappedName);
    }

    default FunctionColumn caseIntervals(double... value){
    	return new FunctionColumn(intervals(value), this, null);
    }

    default FunctionColumn caseIntervals(String mappedName, double... value){
    	return new FunctionColumn(intervals(value), this, mappedName);
    }
    
    default <T> FunctionColumn caseValues(CaseExpressionBuilder<T> cb){
    	return new FunctionColumn(values(cb), this, null);
    }

    default <T> FunctionColumn caseValues(String mappedName, CaseExpressionBuilder<T> cb){
    	return new FunctionColumn(values(cb), this, mappedName);
    }
}
