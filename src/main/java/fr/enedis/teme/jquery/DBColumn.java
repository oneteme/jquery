package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.CaseColumn.betweenIntervals;
import static fr.enedis.teme.jquery.CaseColumn.inValues;
import static fr.enedis.teme.jquery.OperationExpression.equal;
import static fr.enedis.teme.jquery.OperationExpression.greaterOrEquals;
import static fr.enedis.teme.jquery.OperationExpression.greaterThan;
import static fr.enedis.teme.jquery.OperationExpression.in;
import static fr.enedis.teme.jquery.OperationExpression.isNotNull;
import static fr.enedis.teme.jquery.OperationExpression.isNull;
import static fr.enedis.teme.jquery.OperationExpression.lessOrEquals;
import static fr.enedis.teme.jquery.OperationExpression.lessThan;
import static fr.enedis.teme.jquery.OperationExpression.notEquals;
import static fr.enedis.teme.jquery.OperationExpression.notIn;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import java.util.Map.Entry;

public interface DBColumn extends DBObject<DBTable> {

	String getMappedName();

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

	// filters
    default ExpressionColumn nullFilter(){
    	return new ExpressionColumn(this, isNull());
    }
    
    default ExpressionColumn notNullFilter(){
    	return new ExpressionColumn(this, isNotNull());
    }
	
    default ExpressionColumn equalFilter(Object value){
    	return new ExpressionColumn(this, equal(value));
    }
    
    default ExpressionColumn notEqualFilter(Object value){
    	return new ExpressionColumn(this, notEquals(value));
    }

    default <T> ExpressionColumn inFilter(@SuppressWarnings("unchecked") T... values){
    	return new ExpressionColumn(this, in(values));
    }
    
    default <T> ExpressionColumn notInFilter(@SuppressWarnings("unchecked") T... values){
    	return new ExpressionColumn(this, notIn(values));
    }
    
    default ExpressionColumn greaterThanFilter(Object min){
    	return new ExpressionColumn(this, greaterThan(min));
    }

    default  ExpressionColumn greaterOrEqualFilter(Object min){
    	return new ExpressionColumn(this, greaterOrEquals(min));
    }
    
    default  ExpressionColumn lessThanFilter(Object max){
    	return new ExpressionColumn(this, lessThan(max));
    }
    
    default  ExpressionColumn lessOrEqualFilter(Object max){
    	return new ExpressionColumn(this, lessOrEquals(max));
    }
    
    // expressions
    default ExpressionColumn nullExpression(String tagname){
    	return new ExpressionColumn(this, isNull(), tagname);
    }
    
    default ExpressionColumn notNullExpression(String tagname){
    	return new ExpressionColumn(this, isNotNull(), tagname);
    }
	
    default ExpressionColumn equalExpression(String tagname, Object value){
    	return new ExpressionColumn(this, equal(value), tagname);
    }
    
    default ExpressionColumn notEqualExpression(String tagname, Object value){
    	return new ExpressionColumn(this, notEquals(value), tagname);
    }

    default <T> ExpressionColumn inExpression(String tagname, @SuppressWarnings("unchecked") T... values){
    	return new ExpressionColumn(this, in(values), tagname);
    }
    
    default <T> ExpressionColumn notInExpression(String tagname, @SuppressWarnings("unchecked") T... values){
    	return new ExpressionColumn(this, notIn(values), tagname);
    }
    
    default ExpressionColumn greaterThanExpression(String tagname, Object min){
    	return new ExpressionColumn(this, greaterThan(min), tagname);
    }

    default  ExpressionColumn greaterOrEqualExpression(String tagname, Object min){
    	return new ExpressionColumn(this, greaterOrEquals(min), tagname);
    }
    
    default  ExpressionColumn lessThanExpression(String tagname, Object max){
    	return new ExpressionColumn(this, lessThan(max), tagname);
    }
    
    default  ExpressionColumn lessOrEqualExpression(String tagname, Object max){
    	return new ExpressionColumn(this, lessOrEquals(max), tagname);
    }

	// case column
    default CaseColumn caseIntervals(Integer... values){
    	return betweenIntervals(this, values);
    }

    default CaseColumn caseIntervals(String mappedName, Integer... values){
    	return betweenIntervals(this, mappedName, values);
    }

    default CaseColumn caseIntervals(Double... values){
    	return betweenIntervals(this, values);
    }

    default CaseColumn caseIntervals(String mappedName, Double... values){
    	return betweenIntervals(this, mappedName, values);
    }
    
    @SuppressWarnings("unchecked")
	default <T> CaseColumn caseValues(Entry<String, T[]>... values){
    	return inValues(this, values);
    }

    @SuppressWarnings("unchecked")
    default <T> CaseColumn caseValues(String mappedName, Entry<String, T[]>... values){
    	return inValues(this, mappedName, values);
    }
    
}
