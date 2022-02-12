package fr.enedis.teme.jquery;

import java.util.Map;

public interface Query {

	void columns(String alias, SqlStringBuilder sb, QueryParameterBuilder pb, Map<String, String> columnMap);

	void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb);
	
}