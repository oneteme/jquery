package fr.enedis.teme.jquery;

public enum StdFunction implements DBFunction {

	ABS, TRIM, LENGTH, UPPER, LOWER;
	
	@Override
	public String getFunctionName() {
		return name();
	}
	
	@Override
	public boolean isAggregate() {
		return false;
	}
	
}
