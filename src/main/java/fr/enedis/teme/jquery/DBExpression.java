package fr.enedis.teme.jquery;

import java.util.stream.Stream;

public interface DBExpression extends DBObject<String> {

	Stream<Object> args();
	
	/***
	 * 
	 * @return null
	 */

	@Override
	default String tag(String table) {
		return null;
	}

}
