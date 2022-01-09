package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class CaseSingleColumnBuilder {

	private final CaseSingleColumn cases = new CaseSingleColumn();
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
		cases.append(new WhenCase(null, def));
		return cases;
	}
	
	public NamedColumn as(String tagname) {
		return new NamedColumn(tagname, cases);
	}
	
	public final class WhenFilterBridge {
		
		public CaseSingleColumnBuilder then(int value) {
			cases.append(new WhenCase(requireNonNull(filter), value));
			return CaseSingleColumnBuilder.this;
		}

		public CaseSingleColumnBuilder then(double value) {
			cases.append(new WhenCase(requireNonNull(filter), value));
			return CaseSingleColumnBuilder.this;
		}

		public CaseSingleColumnBuilder then(String value) {
			cases.append(new WhenCase(requireNonNull(filter), value));
			return CaseSingleColumnBuilder.this;
		}
		
		public CaseSingleColumnBuilder then(@NonNull TableColumn column) {
			cases.append(new WhenCase(requireNonNull(filter), column));
			return CaseSingleColumnBuilder.this;
		}
		
		public CaseSingleColumnBuilder then(@NonNull Supplier<Object> fn) {
			cases.append(new WhenCase(requireNonNull(filter), fn));
			return CaseSingleColumnBuilder.this;
		}
	}
}
