package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ArithmeticOperator.ADD;
import static fr.enedis.teme.jquery.ArithmeticOperator.DIV;
import static fr.enedis.teme.jquery.ArithmeticOperator.MULT;
import static fr.enedis.teme.jquery.ArithmeticOperator.SUB;
import static fr.enedis.teme.jquery.Validation.requireLegalVariable;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;

import fr.enedis.teme.jquery.CaseSingleColumnBuilder.WhenFilterBridge;
import lombok.NonNull;

@FunctionalInterface
public interface DBColumn extends DBObject<DBTable> {

	default boolean isExpression() {
		return false;
	}

	default boolean isAggregation() {
		return false;
	}

	default boolean isConstant() {
		return false;
	}

	default NamedColumn as(String name) {
		return new NamedColumn(requireLegalVariable(name), this);
	}

	// filters
	default ColumnSingleFilter equal(Object value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.equal(value));
	}

	default ColumnSingleFilter notEqual(Object value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.notEqual(value));
	}

	default ColumnSingleFilter greaterThan(@NonNull Object value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.greaterThan(value));
	}

	default ColumnSingleFilter greaterOrEqual(@NonNull Object value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.greaterOrEqual(value));
	}

	default ColumnSingleFilter lessThan(@NonNull Object value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.lessThan(value));
	}

	default ColumnSingleFilter lessOrEqual(@NonNull Object value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.lessOrEqual(value));
	}

	default ColumnSingleFilter like(@NonNull String value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.like(value));
	}

	default ColumnSingleFilter notLike(@NonNull String value) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.notLike(value));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter in(@NonNull T... values) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.in(values));
	}

	@SuppressWarnings("unchecked")
	default <T> ColumnSingleFilter notIn(@NonNull T... values) {
		return new ColumnSingleFilter(this, OperatorSingleExpression.notIn(values));
	}

	default ColumnSingleFilter isNull() {
		return new ColumnSingleFilter(this, OperatorSingleExpression.isNull());
	}

	default ColumnSingleFilter isNotNull() {
		return new ColumnSingleFilter(this, OperatorSingleExpression.isNotNull());
	}

	default WhenFilterBridge when(OperatorExpression ex) {
		return new CaseSingleColumnBuilder(this).when(ex);
	}

	default ExpressionColumn plus(@NonNull Number value) {
		return new ExpressionColumn(this, staticColumn(value), ADD);
	}
	
	default ExpressionColumn plus(@NonNull DBColumn column) {
		return new ExpressionColumn(this, column, ADD);
	}

	default ExpressionColumn subtract(@NonNull Number value) {
		return new ExpressionColumn(this, staticColumn(value), SUB);
	}
	
	default ExpressionColumn subtract(@NonNull DBColumn column) {
		return new ExpressionColumn(this, column, SUB);
	}

	default ExpressionColumn multiply(@NonNull Number value) {
		return new ExpressionColumn(this, staticColumn(value), MULT);
	}
	
	default ExpressionColumn multiply(@NonNull DBColumn column) {
		return new ExpressionColumn(this, column, MULT);
	}

	default ExpressionColumn divide(@NonNull Number value) {
		return new ExpressionColumn(this, staticColumn(value), DIV);
	}
	
	default ExpressionColumn divide(@NonNull DBColumn column) {
		return new ExpressionColumn(this, column, DIV);
	}
}
