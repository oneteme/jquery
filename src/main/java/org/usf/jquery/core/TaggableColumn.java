package org.usf.jquery.core;

public interface TaggableColumn extends DBColumn {

	String reference(); //JSON & TAG
	
	default String tagSql(QueryParameterBuilder ph) {
		return sql(ph) + " AS " + reference();
	}
}