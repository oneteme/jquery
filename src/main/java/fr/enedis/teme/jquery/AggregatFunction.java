package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;

public enum AggregatFunction implements DBFunction {
	
	COUNT, SUM, AVG, MIN, MAX;
	
	@Override
	public String physicalName() {
		return name();
	}
		
	@Override
	public boolean isAggregate() {
		return true;
	}
	
	public FunctionColumn ofAll() {
		illegalArgumentIf(this != COUNT, "column is required");
		return new FunctionColumn((tab, arg)-> "*", this);
	}

}
