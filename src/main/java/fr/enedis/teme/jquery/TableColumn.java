package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;

public interface TableColumn extends DBColumn {
	
	String name();

	@Override
	default String sql(DBTable table, ParameterHolder arg) {
		return requireNonBlank(table.dbColumnName(this));
	}

	@Override
	default boolean isExpression() {
		return false;
	}

	@Override
	default boolean isAggregation() {
		return false;
	}

	@Override
	default boolean isConstant() {
		return false;
	}

}
