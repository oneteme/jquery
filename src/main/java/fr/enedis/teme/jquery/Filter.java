package fr.enedis.teme.jquery;

import java.util.Collection;

public interface Filter {
	
	String toSql(Table table);
	
	Collection<Object> args();

}
