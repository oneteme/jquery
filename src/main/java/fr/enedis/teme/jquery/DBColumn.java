package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.formatValue;
import static fr.enedis.teme.jquery.Validation.requireLegalVariable;

import fr.enedis.teme.jquery.CaseSingleColumnBuilder.WhenFilterBridge;
import lombok.NonNull;

@FunctionalInterface
public interface DBColumn extends DBObject {

	default boolean isAggregation() {
		return false;
	}

	default boolean isConstant() {
		return false;
	}

	default NamedColumn as(String name) {
		return new NamedColumn(this, requireLegalVariable(name));
	}

	// filters
	default ColumnSingleFilter equal(Object value) {
		return filter(ComparisonSingleExpression.equal(value));
	}

	default ColumnSingleFilter notEqual(Object value) {
		return filter(ComparisonSingleExpression.notEqual(value));
	}

	default ColumnSingleFilter greaterThan(Object value) {
		return filter(ComparisonSingleExpression.greaterThan(value));
	}

	default ColumnSingleFilter greaterOrEqual(Object value) {
		return filter(ComparisonSingleExpression.greaterOrEqual(value));
	}

	default ColumnSingleFilter lessThan(Object value) {
		return filter(ComparisonSingleExpression.lessThan(value));
	}

	default ColumnSingleFilter lessOrEqual(Object value) {
		return filter(ComparisonSingleExpression.lessOrEqual(value));
	}

	default ColumnSingleFilter like(Object value) {
		return filter(ComparisonSingleExpression.like(value));
	}

	default ColumnSingleFilter notLike(Object value) {
		return filter(ComparisonSingleExpression.notLike(value));
	}
	

	default ColumnSingleFilter ilike(Object value) {
		return filter(ComparisonSingleExpression.iLike(value));
	}

	default ColumnSingleFilter notILike(Object value) {
		return filter(ComparisonSingleExpression.notILike(value));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter in(T... values) {
		return filter(ComparisonSingleExpression.in(values));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter notIn(T... values) {
		return filter(ComparisonSingleExpression.notIn(values));
	}

	default ColumnSingleFilter isNull() {
		return filter(ComparisonSingleExpression.isNull());
	}

	default ColumnSingleFilter isNotNull() {
		return filter(ComparisonSingleExpression.isNotNull());
	}

	default ColumnSingleFilter filter(ComparatorExpression exp) {
		return new ColumnSingleFilter(this, exp);
	}
	
	
	default ExpressionColumn plus(Object o) {
		return apply(OperationSingleExpression.plus(o));
	}

	default ExpressionColumn minus(Object o) {
		return apply(OperationSingleExpression.minus(o));
	}

	default ExpressionColumn multiply(Object o) {
		return apply(OperationSingleExpression.multiply(o));
	}

	default ExpressionColumn divise(Object o) {
		return apply(OperationSingleExpression.divise(o));
	}
	
	default ExpressionColumn mode(Object o) {
		return apply(OperationSingleExpression.mode(o));
	}

	default ExpressionColumn pow(Object o) {
		return apply(OperationSingleExpression.pow(o));
	}

	default ExpressionColumn apply(OperationSingleExpression o) {
		return new ExpressionColumn(this, o);
	}

	default WhenFilterBridge when(ComparatorExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}
	
	public static DBColumn ofReference(@NonNull String value) {
		return p-> value;
	}

	static DBColumn ofConstant(Object value) {
		return new DBColumn() {
			
			@Override
			public String sql(QueryParameterBuilder arg) {
				return formatValue(value);
			}
			
			@Override
			public boolean isConstant() {
				return true;
			}
		};
	}
	
	static boolean isColumnConstant(Object o) {
		return !(o instanceof DBColumn && !((DBColumn)o).isConstant());
	}
	
	static boolean isColumnAggregation(Object o) {
		return o instanceof DBColumn && ((DBColumn)o).isAggregation();
	}
	
}
