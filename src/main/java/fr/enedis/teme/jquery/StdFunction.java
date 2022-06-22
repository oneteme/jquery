package fr.enedis.teme.jquery;

public enum StdFunction implements DBFunction {

	ABS, SQRT, TRUNC, CEIL, FLOOR, //numeric functions
	LENGTH, TRIM, UPPER, LOWER; //string functions
	
	@Override
	public String physicalName() {
		return name();
	}
	
	@Override
	public boolean isAggregate() {
		return false;
	}
	
}
