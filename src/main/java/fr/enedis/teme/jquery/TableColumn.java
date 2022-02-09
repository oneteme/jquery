package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;

public interface TableColumn extends TaggableColumn {
	
	String name();

	@Override
	default String sql(DBTable table, QueryParameterBuilder arg) {
		return requireNonBlank(table.physicalColumnName(this));
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
