package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultSetConsumer extends ResultSetMapper<Void> {
	
	void fetch(ResultSet rs) throws SQLException;
	
	@Override
	default Void map(ResultSet rs) throws SQLException {
		this.fetch(rs);
		return null;
	}
}
