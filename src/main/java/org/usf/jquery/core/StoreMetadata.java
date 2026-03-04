package org.usf.jquery.core;

import javax.sql.DataSource;

/**
 * 
 * @author u$f
 * 
 */
public final record StoreMetadata (
	ProductVendor product,
	DataSource dataSource,
	Operators operators, 
	Comparators comparators) {
	
	

}
