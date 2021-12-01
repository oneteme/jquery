package fr.enedis.teme.jquery;

public interface Table {
	
	String getTableName();
	
	Column[] getColumns();
	
	Filter[] getClauses();
	
	String getColumnName(Column column);
	
	//partition table
	//TODO : wait table create
	default String getTableName(Integer year) {
		return getTableName();// + "_" + year;
	}
	
}
