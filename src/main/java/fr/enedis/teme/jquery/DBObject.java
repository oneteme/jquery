package fr.enedis.teme.jquery;

@FunctionalInterface
public interface DBObject<T> {
	
	String sql(T obj, QueryParameterBuilder arg);

}
