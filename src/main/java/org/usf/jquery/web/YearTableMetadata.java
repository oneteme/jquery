package org.usf.jquery.web;

import static java.lang.String.join;
import static java.time.Month.DECEMBER;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class YearTableMetadata extends TableMetadata {
	
	private static final String PATTERN = "_20**";
	
	private final String revisionColumn; //nullable
	private Set<String> tablenames;
	@Getter
	private YearMonth[] revisions;
	
	YearTableMetadata(String tablename, String revisionColumn, Map<String, ColumnMetadata> columns) {
		super(tablename, columns);
		this.revisionColumn = revisionColumn;
	}
	
	@Override
	void fetch(DatabaseMetaData metadata) throws SQLException  {
		var dbMap = getColumns().values().stream().collect(toMap(ColumnMetadata::getColumnName, ColumnMetadata::reset)); //important! reset columns
		Map<String, Set<String>> columnTables = new LinkedHashMap<>();
		Map<String, Set<String>> dirtyColumns = new LinkedHashMap<>();
		try(var rs = metadata.getColumns(null, null, getTablename() + "_20__", null)){
			if(!rs.next()) {
				throw new NoSuchElementException("no tables found with pattern " + getTablename() + PATTERN);
			}
			do {
				var cn = rs.getString("COLUMN_NAME");
				var tn = rs.getString("TABLE_NAME");
				if(dbMap.containsKey(cn) && tn.matches(getTablename()+ "_20\\d{2}")) {// unsafe sql pattern
					columnTables.computeIfAbsent(cn, k-> new LinkedHashSet<>()).add(tn);
					var type = rs.getInt("DATA_TYPE");
					var size = rs.getInt("COLUMN_SIZE");
					var meta = dbMap.get(cn);
					if(meta.getDataType() == AUTO_TYPE) {
						meta.setDataType(type);
						meta.setDataSize(size);
					}
					else if(meta.getDataType() != type || meta.getDataSize() != size) {
						dirtyColumns.computeIfAbsent(tn, k-> new LinkedHashSet<>()).add(cn);
					}
				}//else undeclared column
			} while(rs.next());
		}
		var dirtyDefinition = columnTables.entrySet().stream().filter(e-> e.getValue().equals(getColumns().keySet())).map(Entry::getKey).collect(toList());
		if(!dirtyDefinition.isEmpty()) {
			throw new IllegalStateException("columns [" + join(", ", dirtyDefinition) + "] must be present in all tables " + getTablename() + PATTERN);
		}
		if(!dirtyColumns.isEmpty()) {
			throw new IllegalStateException("columns [" + join(", ", dirtyColumns.keySet()) + "] must have the same definition in all tables " + getTablename() + PATTERN);
		}
		
		this.tablenames = columnTables.values().iterator().next();
	}
	
	void fetchRevisions(Connection cn) { // change this call
		if(isEmpty(tablenames)) {
			return;
		}
		if(isNull(revisionColumn)) { //revision year only 
			this.revisions = tablenames.stream()
					.map(n-> n.substring(n.length()-4))
					.map(Year::parse)
					.sorted(reverseOrder())
					.map(y-> y.atMonth(DECEMBER))
					.toArray(YearMonth[]::new);
		}
		else {
			var query = tablenames.stream()
			.map(t-> "SELECT DISTINCT " + revisionColumn + ", " + t.substring(t.length()-4) + " FROM " + t)
			.collect(joining(" UNION ALL "));
			var yearMonths = new LinkedList<YearMonth>();
			try(var ps = cn.createStatement()){
				log.debug(query);
				try(var rs = ps.executeQuery(query)){
					while(rs.next()) {
						yearMonths.add(YearMonth.of(rs.getInt(2), rs.getInt(1)));
					}
				}
				this.revisions = yearMonths.stream().sorted(reverseOrder()).toArray(YearMonth[]::new); //sort desc.
			}
			catch(SQLException e) {
				log.error(identity() + " : cannot fetch month revisions", e);
				this.revisions = null; 
			}
		}
	}

	public static YearTableMetadata yearTableMetadata(TableDecorator table, String revisionColumn, Collection<ColumnDecorator> columns) {
		var map = new LinkedHashMap<String, ColumnMetadata>();
		columns.stream().forEach(cd-> 
			table.columnName(cd).ifPresent(cn-> 
				map.put(cd.identity(), new ColumnMetadata(cn))));
		return new YearTableMetadata(table.tableName(), revisionColumn, unmodifiableMap(map));
	}

	@Deprecated
	public YearMonth latestRevision() { //optional
		return revisions[0];
	}
}
