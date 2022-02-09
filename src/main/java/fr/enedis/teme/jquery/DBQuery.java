package fr.enedis.teme.jquery;

public interface DBQuery {
	
	public void sql(StringBuilder sb, QueryColumnBuilder cb, QueryParameterBuilder pb, String alias);

}
