package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author u$f
 *
 */
public interface RowMapper<T> extends ResultSetMapper<List<T>>  {
	
	T mapRow(ResultSet rs) throws SQLException;
	
	@Override
	default List<T> map(ResultSet rs) throws SQLException {
		List<T> arr = new ArrayList<>();
		while(rs.next()) {
			arr.add(mapRow(rs));
		}
		return arr;
	}
}
