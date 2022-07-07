package fr.enedis.teme.jquery;

public interface Query {

	void build(String schema, SqlStringBuilder sb, QueryParameterBuilder pb);
	
}