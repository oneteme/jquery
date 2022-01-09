package fr.enedis.teme.jquery.builder;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;

import java.util.function.Supplier;

import fr.enedis.teme.jquery.DBColumn;
import fr.enedis.teme.jquery.DBFilter;
import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.LogicalOperator;
import fr.enedis.teme.jquery.ParameterHolder;
import fr.enedis.teme.jquery.TableColumn;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ColumnFilterBridge implements DBColumn {
	
	private final WhenCaseBuilder builder;
	private final DBColumn column;
	private final FilterProxy filterProxy = new FilterProxy();
 	
	@Override
	public FilterProxy equal(Object value) { 
		return filterProxy.append(column.equal(value));
	}

	@Override
	public FilterProxy notEqual(Object value) {
		return filterProxy.append(column.notEqual(value));
	}

	@Override
	public FilterProxy greaterThan(Object min) {
		return filterProxy.append(column.greaterThan(min));
	}

	@Override
	public FilterProxy greaterOrEqual(Object min) {
		return filterProxy.append(column.greaterOrEqual(min));
	}

	@Override
	public FilterProxy lessThan(Object max) {
		return filterProxy.append(column.lessThan(max));
	}

	@Override
	public FilterProxy lessOrEqual(Object max) {
		return filterProxy.append(column.lessOrEqual(max));
	}

	@Override
	public FilterProxy like(String value) {
		return filterProxy.append(column.like(value));
	}

	@Override
	public FilterProxy notLike(String value) {
		return filterProxy.append(column.notLike(value));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> FilterProxy in(T... values) {
		return filterProxy.append(column.in(values));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> FilterProxy notIn(T... values) {
		return filterProxy.append(column.notIn(values));
	}

	@Override
	public FilterProxy isNull() {
		return filterProxy.append(column.isNull());
	}

	@Override
	public FilterProxy isNotNull() {
		return filterProxy.append(column.isNotNull());
	}
	
	@Override
	public String sql(DBTable obj, ParameterHolder arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExpression() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAggregation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConstant() {
		throw new UnsupportedOperationException();
	}
	
	public final class FilterProxy implements DBFilter {

		private DBFilter filter;
		private LogicalOperator op;
		
		FilterProxy append(DBFilter f) {
			if(this.filter == null) {
				this.filter = f;
			}
			else {
				if(op == AND) {
					this.filter = this.filter.and(f);
				}
				else if(op == OR) {
					this.filter = this.filter.or(f);
				}
				op = null;
			}
			return this;
		}

		public ColumnFilterBridge and() {
			this.op = AND;
			return ColumnFilterBridge.this;
		}

		public ColumnFilterBridge or() {
			this.op = OR; 
			return ColumnFilterBridge.this;
		}

		@Override
		public FilterProxy and(DBFilter filter) {
			this.filter = filter.and(filter);
			return this;
		}

		@Override
		public FilterProxy or(DBFilter filter) {
			this.filter = filter.or(filter);
			return this;
		}

		@Override
		public WhenCaseBuilder then(int value) {
			return builder.append(filter.then(value));
		}

		@Override
		public WhenCaseBuilder then(double value) {
			return builder.append(filter.then(value));
		}

		@Override
		public WhenCaseBuilder then(String value) {
			return builder.append(filter.then(value));
		}

		@Override
		public WhenCaseBuilder then(TableColumn column) {
			return builder.append(filter.then(column));
		}

		@Override
		public WhenCaseBuilder then(Supplier<Object> fn) {
			return builder.append(filter.then(fn));
		}
		
		@Override
		public String sql(DBTable obj, ParameterHolder arg) {
			throw new UnsupportedOperationException();
		}
	}
}
