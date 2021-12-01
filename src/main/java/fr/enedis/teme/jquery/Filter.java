package fr.enedis.teme.jquery;

import java.util.Collection;

public interface Filter {
	
	Collection<Object> args();
	
	String toSql(Table table);

}
