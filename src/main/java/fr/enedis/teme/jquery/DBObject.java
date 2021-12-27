package fr.enedis.teme.jquery;

public interface DBObject<T> {
	
	String sql(T obj, ParameterHolder arg);

}
