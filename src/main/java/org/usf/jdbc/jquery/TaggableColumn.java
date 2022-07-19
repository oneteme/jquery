package org.usf.jdbc.jquery;

public interface TaggableColumn extends DBColumn {

	String tagname();
	
	default String tagSql(QueryParameterBuilder ph) {
		return sql(ph) + " AS " + tagname();
	}
}
