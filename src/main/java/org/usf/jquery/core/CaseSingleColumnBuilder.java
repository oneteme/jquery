package org.usf.jquery.core;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
@AllArgsConstructor
public final class CaseSingleColumnBuilder {

	private final CaseColumn caseColumn = new CaseColumn();
	private final WhenFilterBridge bridge = new WhenFilterBridge(); 
	private final DBColumn column;
	private DBFilter filter; //temp
	
	public CaseSingleColumnBuilder(@NonNull DBColumn column) {
		this.column = column;
	}
	
	public WhenFilterBridge when(ComparisonExpression exp) {
		this.filter = new ColumnSingleFilter(column, exp);
		return bridge;
	}
	
	public CaseColumn orElse(int value) {
		return orElseExp(value);
	}

	public CaseColumn orElse(double value) {
		return orElseExp(value);
	}

	public CaseColumn orElse(String value) {
		return orElseExp(value);
	}
	
	public CaseColumn orElse(DBColumn column) {
		return orElseExp(column);
	}
	
	public CaseColumn end() {
		return caseColumn;
	}
	
	private CaseColumn orElseExp(Object elseValue) {
		caseColumn.append(WhenCase.orElse(elseValue));
		return caseColumn;
	}
	
	public ColumnProxy as(String tagname) {
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
			caseColumn.append(new WhenCase(requireNonNull(filter), o));
			filter = null;
			return CaseSingleColumnBuilder.this;			
		}
	}
}
