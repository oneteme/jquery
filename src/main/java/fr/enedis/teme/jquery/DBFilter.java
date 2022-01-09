package fr.enedis.teme.jquery;

import java.util.function.Supplier;

public interface DBFilter extends DBObject<DBTable> {
	
	DBFilter and(DBFilter filter);

	DBFilter or(DBFilter filter);	
	
	default WhenExpression then(int value) {
		return new WhenCase(this, value);
	}

	default WhenExpression then(double value) {
		return new WhenCase(this, value);
	}

	default WhenExpression then(String value) {
		return new WhenCase(this, value);
	}
	
	default WhenExpression then(TableColumn column) {
		return new WhenCase(this, column);
	}
	
	default WhenExpression then(Supplier<Object> fn) {
		return new WhenCase(this, fn);
	}

}
