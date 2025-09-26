package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author u$f
 *
 */
public interface RowMapper<T> {
	
	T mapRow(ResultSet rs, int row) throws SQLException;
}
