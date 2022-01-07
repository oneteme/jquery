package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.formatValue;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WhenCase {
	
	private final DBFilter filter;
	private final Object value;
	
	public String sql(DBTable table, ParameterHolder arg) {
		
		var sb = new StringBuilder(50);
		if(filter == null) {
			sb.append("ELSE ");
		}
		else {
			sb.append("WHEN ").append(filter.sql(table, arg)).append(" THEN ");
		}
		if(value instanceof Supplier) {
			sb.append(((Supplier<?>)value).get()); //function
		}
		else {
			sb.append(value instanceof TableColumn 
					? ((DBColumn)value).sql(table, arg)//tableColumn or Expression
					: formatValue(value)); //constant
		}
		return sb.toString();
	}

	public CaseColumn2 orElse(int value) {
		return orElseExp(value);
	}

	public CaseColumn2 orElse(double value) {
		return orElseExp(value);
	}

	public CaseColumn2 orElse(String value) {
		return orElseExp(value);
	}
	
	public CaseColumn2 orElse(Supplier<Object> fn) {
		return orElseExp(fn);
	}

	private CaseColumn2 orElseExp(Object whenCase) {
		return new CaseColumn2(null, this, new WhenCase(null, whenCase));
	}
	
}
