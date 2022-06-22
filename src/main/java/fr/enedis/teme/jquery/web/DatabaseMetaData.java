package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.web.ParameterInvalidValueException.invalidParameterValueException;
import static java.util.Collections.emptyMap;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.TableColumn;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@NoArgsConstructor
public final class DatabaseMetaData {
	
	private Map<String, TableMetadata> tables = emptyMap();
	
	void setTables(@NonNull Map<String, TableMetadata> tables){
		this.tables = tables;
	}
	
	public YearMonth[] filterExistingRevision(DBTable table, Stream<YearMonth> revs) {
		var meta = tables.get(table.physicalName());
		if(meta != null && meta.getRevisions().length > 0) {
			return revs.filter(r-> {
				if(meta.exists(r.getYear())) {
					return true;
				}
				log.warn(table.physicalName() + "#" + r.getYear() + " : partition not found");
				return false;
			}).toArray(YearMonth[]::new);
		}
		log.warn(table.physicalName() + " : empty partitions");
		return new YearMonth[] {}; //empty
	}

	public Object typedValue(DBTable table, TableColumn column, String value) {
		var tm = tables.get(table.physicalName());
		if(tm != null) {
			var cm = tm.getColumns().get(table.physicalColumnName(column));
			if(cm != null) {
				try {
					return cm.parser().apply(value);
				}
				catch(Exception e) {
					throw invalidParameterValueException(value, e);
				}
			}
		}
		log.error("column metadata not found for : " + table.physicalName() + "." + table.physicalColumnName(column));
		return value;
	}

	public Object[] typedValues(DBTable table, TableColumn column, String[] values) {

		var tm = tables.get(table.physicalName());
		if(tm != null) {
			var cm = tm.getColumns().get(table.physicalColumnName(column));
			if(cm != null) {
				var fn = cm.parser();
				List<Object> list = new ArrayList<>(values.length);
				for(String value: values) {
					try {
						list.add(fn.apply(value));
					}
					catch(Exception e) {
						throw invalidParameterValueException(value, e);
					}
				}
				return list.toArray();
			}
		}
		log.error("column metadata not found for : " + table.physicalName() + "." + table.physicalColumnName(column));
		return values;
	}
	
	public YearMonth currentRevision(DBTable table) {
		return tables.get(table.physicalName()).currentRevision();
	}
	
	
	
	//max size check
}