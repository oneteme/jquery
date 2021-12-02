package fr.enedis.teme.jquery;

import java.util.Collection;

public interface DBFilter extends DBObject<DBTable> {
	
	Collection<Object> args();

}
