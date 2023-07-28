package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultMapper<T> {

    T map(ResultSet rs) throws SQLException;
	
	default String[] declaredColumns(ResultSet rs) throws SQLException {
		var names = new String[rs.getMetaData().getColumnCount()];
		for(var i=0; i<names.length; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
		}
		return names;
	}

}