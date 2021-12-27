package fr.enedis.teme.jquery;

public interface DBFilter extends DBObject<DBTable> {

	default NamedFilter as(String name) { //map
		return new NamedFilter(name, this);
	}

}
