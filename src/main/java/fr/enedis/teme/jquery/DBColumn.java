package fr.enedis.teme.jquery;

import fr.enedis.teme.jquery.CaseSingleColumnBuilder.WhenFilterBridge;
import lombok.NonNull;

public interface DBColumn extends DBObject<DBTable> {
	
	boolean isExpression();
	
	boolean isAggregation();
	
	boolean isConstant();

	default NamedColumn as(String name) {
		return new NamedColumn(name, this);
	}
	
	// filters	
    default ColumnSingleFilter equal(Object value){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.equal(value));
    }
    
    default ColumnSingleFilter notEqual(Object value){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.notEqual(value));
    }
    
    default ColumnSingleFilter greaterThan(@NonNull Object value){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.greaterThan(value));
    }

    default ColumnSingleFilter greaterOrEqual(@NonNull Object value){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.greaterOrEqual(value));
    }
    
    default ColumnSingleFilter lessThan(@NonNull Object value){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.lessThan(value));
    }
    
    default ColumnSingleFilter lessOrEqual(@NonNull Object value){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.lessOrEqual(value));
    }

	default ColumnSingleFilter like(@NonNull String value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.like(value));
	}

	default ColumnSingleFilter notLike(@NonNull String value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.notLike(value));
	}

    @SuppressWarnings("unchecked")
    default <T> ColumnSingleFilter in(@NonNull T... values){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.in(values));
    }
    
    @SuppressWarnings("unchecked")
    default <T> ColumnSingleFilter notIn(@NonNull T... values){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.notIn(values));
    }

    default ColumnSingleFilter isNull(){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.isNull());
    }
    
    default ColumnSingleFilter isNotNull(){
    	return new ColumnSingleFilter(this, OperatorSingleExpression.isNotNull());
    }
    
    default WhenFilterBridge when(OperatorExpression ex) {
    	return new CaseSingleColumnBuilder(this).when(ex);
	}
}
