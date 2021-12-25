package fr.enedis.teme.jquery;

import java.util.stream.Stream;

public interface DBFilter extends DBObject<DBTable> {
	
	Stream<Object> args();

}
