package fr.enedis.teme.jquery;

public enum AggregatFunction implements DBExpression {
	
	COUNT, SUM, AVG, MIN, MAX;
	
	@Override
	public String getFunctionName() {
		return name();
	}
	
	@Override
	public boolean isAggregation() {
		return true;
	}

}
