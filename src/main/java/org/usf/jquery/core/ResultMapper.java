package org.usf.jquery.core;


import java.io.IOException;
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
	
	interface RowWriter {
		
		void writeLine(String s) throws IOException;
		
		static RowWriter lineSeparator(RowWriter w) {
			return lineSeparator(w, System.lineSeparator());
		}
		
		static RowWriter lineSeparator(RowWriter w, String separator) {
			return s-> w.writeLine(s + separator);
		}
	}
}