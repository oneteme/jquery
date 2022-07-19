package org.usf.jquery;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public final class CaseSingleColumnBuilder {

	private final CaseSingleColumn caseColumn = new CaseSingleColumn();
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
	
	public CaseSingleColumn orElse(int value) {
		return orElseExp(value);
	}

	public CaseSingleColumn orElse(double value) {
		return orElseExp(value);
	}

	public CaseSingleColumn orElse(String value) {
		return orElseExp(value);
	}
	
	public CaseSingleColumn orElse(DBColumn column) {
		return orElseExp(column);
	}
	
	public CaseSingleColumn end() {
		return caseColumn;
	}
	

	private CaseSingleColumn orElseExp(Object elseValue) {
		caseColumn.append(CaseExpression.orElse(elseValue));
		return caseColumn;
	}
	
	public NamedColumn as(String tagname) {
		return new NamedColumn(caseColumn, tagname);
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
			caseColumn.append(new CaseExpression(requireNonNull(filter), o));
			filter = null;
			return CaseSingleColumnBuilder.this;			
		}
	}
}
