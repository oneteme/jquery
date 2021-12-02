package fr.enedis.teme.jquery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DBFunction implements Function {

	COUNT {
		
		@Override
		public boolean requiredColumn() {
			return false;
		}
		
		@Override
		public String getMappedName() {
			return "nb"; //optional column
		}
		
		@Override
		public String toSql(Table table, Column column) {
			return column == null ? "COUNT(*)" : super.toSql(table, column);
		}
	},

	SUM, MIN, MAX,

	TRIM(false), ABS(false), UPPER(false), LOWER(false);
	
	private final boolean aggregation;
	
	private DBFunction() {
		this.aggregation = true; //true as default 
	}
	
	@Override
	public String getColumnName() {
		return name();
	}
	
	@Override
	public String getMappedName() {
		return name().toLowerCase();
	}

}
