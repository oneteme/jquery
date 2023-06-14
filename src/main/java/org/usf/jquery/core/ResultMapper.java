package org.usf.jquery.core;

import java.sql.ResultSet;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultMapper<T> {
	
	default void declaredColumns(String[] columnNames) { }

    T map(ResultSet rs);

}