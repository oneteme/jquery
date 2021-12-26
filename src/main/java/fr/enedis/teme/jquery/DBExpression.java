package fr.enedis.teme.jquery;

public interface DBExpression extends DBObject<String> {

	@Override
	default String tag(String table) {
		return null;
	}

}
