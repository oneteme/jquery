package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultSetMapper<T> {
	
    T map(ResultSet rs) throws SQLException;
    
    default T mapUnchecked(ResultSet rs) {
		try {
			return map(rs);
		} catch (SQLException e) {
            throw new DataMappingException("Error extracting data from ResultSet", e);
        } catch (Exception e) {
            throw new DataMappingException("Unexpected error during row mapping", e);
        }
	}
	
	static String[] columnNames(ResultSet rs) throws SQLException {
		var names = new String[rs.getMetaData().getColumnCount()];
		for(var i=0; i<names.length; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
		}
		return names;
	}
}