package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.CompareOperator.EQ;
import static fr.enedis.teme.jquery.CompareOperator.GE;
import static fr.enedis.teme.jquery.CompareOperator.GT;
import static fr.enedis.teme.jquery.CompareOperator.IN;
import static fr.enedis.teme.jquery.CompareOperator.IS_NOT_NULL;
import static fr.enedis.teme.jquery.CompareOperator.IS_NULL;
import static fr.enedis.teme.jquery.CompareOperator.LE;
import static fr.enedis.teme.jquery.CompareOperator.LIKE;
import static fr.enedis.teme.jquery.CompareOperator.LT;
import static fr.enedis.teme.jquery.CompareOperator.NE;
import static fr.enedis.teme.jquery.CompareOperator.NOT_IN;
import static fr.enedis.teme.jquery.CompareOperator.NOT_LIKE;

import fr.enedis.teme.jquery.builder.ColumnFilterBridge;
import fr.enedis.teme.jquery.builder.WhenCaseBuilder;
import lombok.NonNull;

public interface DBColumn extends DBObject<DBTable> {
	
	boolean isExpression();
	
	boolean isAggregation();
	
	boolean isConstant();

	default NamedColumn as(String name) {
		return new NamedColumn(name, this);
	}
	
	// filters	
    default DBFilter equal(Object value){
    	return new ColumnFilter(this, EQ, value);
    }
    
    default DBFilter notEqual(Object value){
    	return new ColumnFilter(this, NE, value);
    }
    
    default DBFilter greaterThan(@NonNull Object value){
    	return new ColumnFilter(this, GT, value);
    }

    default DBFilter greaterOrEqual(@NonNull Object value){
    	return new ColumnFilter(this, GE, value);
    }
    
    default DBFilter lessThan(@NonNull Object value){
    	return new ColumnFilter(this, LT, value);
    }
    
    default DBFilter lessOrEqual(@NonNull Object value){
    	return new ColumnFilter(this, LE, value);
    }

	default DBFilter like(@NonNull String value) {
		return new ColumnFilter(this, LIKE, value);
	}

	default DBFilter notLike(@NonNull String value) {
		return new ColumnFilter(this, NOT_LIKE, value);
	}

    @SuppressWarnings("unchecked")
    default <T> DBFilter in(@NonNull T... values){
    	return new ColumnFilter(this, IN, values);
    }
    
    @SuppressWarnings("unchecked")
    default <T> DBFilter notIn(@NonNull T... values){
    	return new ColumnFilter(this, NOT_IN, values);
    }

    default DBFilter isNull(){
    	return new ColumnFilter(this, IS_NULL, null);
    }
    
    default DBFilter isNotNull(){
    	return new ColumnFilter(this, IS_NOT_NULL, null);
    }
    
    default ColumnFilterBridge when() {
	   return new WhenCaseBuilder(this).when();
	}
}
