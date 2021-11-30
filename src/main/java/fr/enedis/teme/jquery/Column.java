package fr.enedis.teme.jquery;

public interface Column {

	String getMappedName();
	
	String getColumnName(Table table);
	
	String toSql(Table table);
	
	default boolean isAggregated() {
		return false;
	}

	// filters
    default NullFilter isNull(){
    	return new NullFilter(this);
    }
    
    default NullFilter isNotNull(){
    	return new NullFilter(this, true);
    }
	
    default <T> InFilter<T> same(T value){
    	return new InFilter<>(this, false, value);
    }
    
    default <T> InFilter<T> notSame(T value){
    	return new InFilter<>(this, true, value);
    }

    default <T> InFilter<T> in(@SuppressWarnings("unchecked") T... values){
    	return new InFilter<>(this, false, values);
    }
    
    default <T> InFilter<T> notIn(@SuppressWarnings("unchecked") T... values){
    	return new InFilter<>(this, true, values);
    }

    default <T> IntervalFilter<T> between(T min, T max){
    	return new IntervalFilter<>(this, min, max);
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

}
