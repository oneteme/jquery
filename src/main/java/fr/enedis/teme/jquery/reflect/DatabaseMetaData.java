package fr.enedis.teme.jquery.reflect;

import static fr.enedis.teme.jquery.web.InvalidParameterValueException.invalidParameterValueException;
import static fr.enedis.teme.jquery.web.ResourceNotFoundException.tableNotFoundException;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.TableColumn;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public class DatabaseMetaData {
	
	private Map<String, TableMetadata> tables = Collections.emptyMap();
	
	void setTables(@NonNull Map<String, TableMetadata> tables){
		this.tables = tables;
	}
	
	public YearMonth requireRevision(DBTable table, YearMonth ym) {
		var meta = tables.get(table.getTableName());
		if(meta != null && IntStream.of(meta.getRevisions()).noneMatch(v-> v == ym.getYear())) {
			throw tableNotFoundException(table.sql(null, ""+ym.getYear(), null));
		}
		return ym;
	}

	public Object typedValue(DBTable table, TableColumn column, String value) {
		var tm = tables.get(table.getTableName());
		if(tm != null) {
			var cm = tm.getColumns().get(table.dbColumnName(column));
			if(cm != null) {
				try {
					return cm.parseValue(value);
				}
				catch(Exception e) {
					throw invalidParameterValueException(value, e);
				}
			}
		}
		return value;
	}

	public Object[] typedValues(DBTable table, TableColumn column, String[] values) {

		var tm = tables.get(table.getTableName());
		if(tm != null) {
			var cm = tm.getColumns().get(table.dbColumnName(column));
			if(cm != null) {
				List<Object> list = new ArrayList<>(values.length);
				for(String value: values) {
					try {
						list.add(cm.parseValue(value));
					}
					catch(Exception e) {
						throw invalidParameterValueException(value, e);
					}
				}
				return list.toArray();
			}
		}
		return values;
	}
	//max size check
}