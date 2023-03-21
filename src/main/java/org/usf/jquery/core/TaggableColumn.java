package org.usf.jquery.core;

public interface TaggableColumn extends DBColumn {

	String reference(); //JSON
	
	default String tagSql(QueryParameterBuilder ph) {
		return sql(ph) + " AS " + reference();
	}
}