package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBColumn.ofReference;
import static fr.enedis.teme.jquery.SqlStringBuilder.parenthese;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;

public enum AggregatFunction implements DBFunction {
	
	COUNT, SUM, AVG, MIN, MAX;
	
	@Override
	public String sql(QueryParameterBuilder ph, Object operand, Object... args) {
		illegalArgumentIf(operand == null || !isEmpty(args),  ()-> this.name() + " require one parameter");
		if(this == COUNT || this == MIN || this == MAX) {
			return this.name() + parenthese(ph.appendParameter(operand));
		}
		else if(this == SUM || this == AVG) {
			return this.name() + parenthese(ph.appendNumber(operand));
		}
		throw new UnsupportedOperationException("Unsupported operator " + this.name());
	}
		
	@Override
	public boolean isAggregate() {
		return true;
	}
	
	public FunctionColumn ofAll() {
		illegalArgumentIf(this != COUNT, ()-> "column is required");
		return new FunctionColumn(this, ofReference("*"));
	}
	
}
