package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;

public enum AggregatFunction implements DBFunction {
	
	COUNT, SUM, AVG, MIN, MAX;
	
	@Override
	public String getFunctionName() {
		return name();
	}
	
	@Override
	public boolean isAggregate() {
		return true;
	}
	
	public FunctionColumn ofAll() {
		illegalArgumentIf(this != COUNT, "Parameter required");
		return new FunctionColumn(staticColumn("all", "*"), this);
	}

}
