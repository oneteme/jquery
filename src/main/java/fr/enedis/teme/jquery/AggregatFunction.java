package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNullElse;

public enum AggregatFunction implements DBFunction {
	
	COUNT {
		@Override
		public String toSql(String columnName) {
			return super.toSql(requireNonNullElse(columnName, "*"));
		}
	}, 
	SUM, AVG, MIN, MAX;
	
	@Override
	public String getFunctionName() {
		return name();
	}
	
	@Override
	public boolean isAggregation() {
		return true;
	}

}
