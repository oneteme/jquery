package fr.enedis.teme.jquery.web;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.TableColumn;

public interface YearPartitionTable extends DBTable {
	
	TableColumn getRevisionColumn();

}
