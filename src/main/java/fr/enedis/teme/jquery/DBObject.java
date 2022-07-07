package fr.enedis.teme.jquery;

@FunctionalInterface
public interface DBObject {
	
	String sql(QueryParameterBuilder arg);

}
