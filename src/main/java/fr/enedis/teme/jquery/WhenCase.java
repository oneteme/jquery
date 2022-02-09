package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static fr.enedis.teme.jquery.QueryParameterBuilder.formatValue;

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WhenCase implements WhenExpression {
	
	private final DBFilter filter;
	private final Object value;
	
	@Override
	public String sql(DBTable table, QueryParameterBuilder arg) {
		
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
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
	public static WhenCase orElse(Object value) {
		return new WhenCase(null, value);
	}
	
}
