package org.usf.jquery.core;

import static org.usf.jquery.core.DatabaseVendor.parseName;

import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
public interface Environment {
	
	static SimpleEnvironment NO_ENV = new SimpleEnvironment(null, null, null);

	DataSource getDataSource();
	
	DatabaseVendor getProduct();
	
	String getSchema();
	
	static SimpleEnvironment using(DataSource ds, String schema) {
		try(var cnx = ds.getConnection()){
			return new SimpleEnvironment(ds, parseName(null), schema);
		}
		catch (SQLException e) {
			throw new JQueryException(e);
		}
	}
	
	@Getter
	@RequiredArgsConstructor
	static class SimpleEnvironment implements Environment {
		
		private final DataSource dataSource;
		private final DatabaseVendor product;
		private final String schema;
	}
}
