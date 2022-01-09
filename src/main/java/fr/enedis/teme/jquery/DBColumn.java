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
    default ColumnFilter equal(Object value){
    	return new ColumnFilter(this, OperatorSingleExpression.equal(value));
    }
    
    default ColumnFilter notEqual(Object value){
    	return new ColumnFilter(this, OperatorSingleExpression.notEqual(value));
    }
    
    default ColumnFilter greaterThan(@NonNull Object value){
    	return new ColumnFilter(this, OperatorSingleExpression.greaterThan(value));
    }

    default ColumnFilter greaterOrEqual(@NonNull Object value){
    	return new ColumnFilter(this, OperatorSingleExpression.greaterOrEqual(value));
    }
    
    default ColumnFilter lessThan(@NonNull Object value){
    	return new ColumnFilter(this, OperatorSingleExpression.lessThan(value));
    }
    
    default ColumnFilter lessOrEqual(@NonNull Object value){
    	return new ColumnFilter(this, OperatorSingleExpression.lessOrEqual(value));
    }

	default ColumnFilter like(@NonNull String value) {
		return new ColumnFilter(this, OperatorSingleExpression.like(value));
	}

	default ColumnFilter notLike(@NonNull String value) {
		return new ColumnFilter(this, OperatorSingleExpression.notLike(value));
	}

    @SuppressWarnings("unchecked")
    default <T> ColumnFilter in(@NonNull T... values){
    	return new ColumnFilter(this, OperatorSingleExpression.in(values));
    }
    
    @SuppressWarnings("unchecked")
    default <T> ColumnFilter notIn(@NonNull T... values){
    	return new ColumnFilter(this, OperatorSingleExpression.notIn(values));
    }

    default ColumnFilter isNull(){
    	return new ColumnFilter(this, OperatorSingleExpression.isNull());
    }
    
    default ColumnFilter isNotNull(){
    	return new ColumnFilter(this, OperatorSingleExpression.isNotNull());
    }
    
    default WhenFilterBridge when(OperatorExpression ex) {
    	return new CaseSingleColumnBuilder(this).when(ex);
	}
}
