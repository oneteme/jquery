package fr.enedis.teme.jquery;

public interface DBObject<T> {
	
	String toSql(T obj);

}
