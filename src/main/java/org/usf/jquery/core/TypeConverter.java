package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface TypeConverter<T> {
	
	Object convert(T value, JDBCType type);
}
