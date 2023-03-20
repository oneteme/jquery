package org.usf.jquery.core;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public final class CaseSingleColumnBuilder {

	private final CaseExpressionColumn caseColumn = new CaseExpressionColumn();
	private final WhenFilterBridge bridge = new WhenFilterBridge(); 
	private final DBColumn column;
	private DBFilter filter; //temp
	
	public CaseSingleColumnBuilder(@NonNull DBColumn column) {
		this.column = column;
	}
	
	public WhenFilterBridge when(ComparatorExpression exp) {
		this.filter = new ColumnSingleFilter(column, exp);
		return bridge;
	}
	
	public CaseExpressionColumn orElse(int value) {
		return orElseExp(value);
	}

	public CaseExpressionColumn orElse(double value) {
		return orElseExp(value);
	}

	public CaseExpressionColumn orElse(String value) {
		return orElseExp(value);
	}
	
	public CaseExpressionColumn orElse(DBColumn column) {
		return orElseExp(column);
	}
	
	public CaseExpressionColumn end() {
		return caseColumn;
	}
	

	private CaseExpressionColumn orElseExp(Object elseValue) {
		caseColumn.append(WhenExpression.orElse(elseValue));
		return caseColumn;
	}
	
	public NamedColumn as(String tagname) {
		return caseColumn.as(tagname);
	}
	
	public final class WhenFilterBridge {

		public CaseSingleColumnBuilder thenNull() {
			return thenExp(null);
		}
		
		public CaseSingleColumnBuilder then(int value) {
			return thenExp(value);
		}

		public CaseSingleColumnBuilder then(double value) {
			return thenExp(value);
		}

		public CaseSingleColumnBuilder then(String value) {
			return thenExp(value);
		}
		
		public CaseSingleColumnBuilder then(@NonNull DBColumn column) {
			return thenExp(column);
		}
		
		public CaseSingleColumnBuilder then(@NonNull Supplier<Object> fn) {
			return thenExp(fn);
		}
		
		private CaseSingleColumnBuilder thenExp(Object o) {
			caseColumn.append(new WhenExpression(requireNonNull(filter), o));
			filter = null;
			return CaseSingleColumnBuilder.this;			
		}
	}
}
