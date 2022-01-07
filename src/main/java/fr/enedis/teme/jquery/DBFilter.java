package fr.enedis.teme.jquery;

import java.util.function.Supplier;

public interface DBFilter extends DBObject<DBTable> {

	@Deprecated
	default WhenCase as(String name) { //map
		return new WhenCase(this, name);
	}

	default WhenCase then(int value) {
		return new WhenCase(this, value);
	}

	default WhenCase then(double value) {
		return new WhenCase(this, value);
	}

	default WhenCase then(String value) {
		return new WhenCase(this, value);
	}
	
	default WhenCase then(TableColumn column) {
		return new WhenCase(this, column);
	}
	
	default WhenCase then(Supplier<Object> fn) {
		return new WhenCase(this, fn);
	}


}
