package fr.enedis.teme.jquery;

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

}
