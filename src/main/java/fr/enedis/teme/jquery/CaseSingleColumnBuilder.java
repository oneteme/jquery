package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class CaseSingleColumnBuilder {

	private final CaseSingleColumn caseColumn = new CaseSingleColumn();
	private final WhenFilterBridge bridge = new WhenFilterBridge(); 
	private final DBColumn column;
	private DBFilter filter; //temp
	
	public CaseSingleColumnBuilder(@NonNull DBColumn column) {
		this.column = column;
	}
	
	public WhenFilterBridge when(OperatorExpression exp) {
		this.filter = new ColumnFilter(column, exp);
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
	
	public CaseSingleColumn orElse(Supplier<Object> fn) {
		return orElseExp(fn);
	}

	public CaseSingleColumn orElseExp(Object def) {
		caseColumn.append(new WhenCase(null, def));
		return caseColumn;
	}
	
	public NamedColumn as(String tagname) {
		return new NamedColumn(tagname, caseColumn);
	}
	
	public final class WhenFilterBridge {
		
		public CaseSingleColumnBuilder then(int value) {
			caseColumn.append(new WhenCase(requireNonNull(filter), value));
			return CaseSingleColumnBuilder.this;
		}

		public CaseSingleColumnBuilder then(double value) {
			caseColumn.append(new WhenCase(requireNonNull(filter), value));
			return CaseSingleColumnBuilder.this;
		}

		public CaseSingleColumnBuilder then(String value) {
			caseColumn.append(new WhenCase(requireNonNull(filter), value));
			return CaseSingleColumnBuilder.this;
		}
		
		public CaseSingleColumnBuilder then(@NonNull TableColumn column) {
			caseColumn.append(new WhenCase(requireNonNull(filter), column));
			return CaseSingleColumnBuilder.this;
		}
		
		public CaseSingleColumnBuilder then(@NonNull Supplier<Object> fn) {
			caseColumn.append(new WhenCase(requireNonNull(filter), fn));
			return CaseSingleColumnBuilder.this;
		}
	}
}
