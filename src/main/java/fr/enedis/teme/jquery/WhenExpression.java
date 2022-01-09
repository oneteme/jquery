package fr.enedis.teme.jquery;

import java.util.function.Supplier;

public interface WhenExpression extends DBExpression {

	default CaseColumn orElse(int value) {
		return orElseExp(value);
	}

	default CaseColumn orElse(double value) {
		return orElseExp(value);
	}

	default CaseColumn orElse(String value) {
		return orElseExp(value);
	}
	
	default CaseColumn orElse(Supplier<Object> fn) {
		return orElseExp(fn);
	}

	default CaseColumn orElseExp(Object def) {
		return new CaseColumn(this, new WhenCase(null, def));
	}
	
}
