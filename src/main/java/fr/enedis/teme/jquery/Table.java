package fr.enedis.teme.jquery;

public interface Table {
	
	String getTableName();
	
	Column[] getColumns();
	
	Filter[] getClauses();
	
	String getColumnName(Column column);
	
}
