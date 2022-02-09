package fr.enedis.teme.jquery;

public interface TaggableColumn extends DBColumn {

	String tagname();
	
	default String tagSql(DBTable table, QueryParameterBuilder ph) {
		return sql(table, ph) + " AS " + tagname();
	}
}
