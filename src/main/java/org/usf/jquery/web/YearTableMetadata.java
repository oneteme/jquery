package org.usf.jquery.web;

import static java.lang.String.join;
import static java.time.Month.DECEMBER;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.EMPTY_REVISION;
import static org.usf.jquery.web.JQueryContext.database;
import static org.usf.jquery.web.ParsableJDBCType.AUTO_TYPE;
import static org.usf.jquery.web.ParsableJDBCType.typeOf;

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
import java.util.Optional;
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
	private final Set<String> tablenames = new LinkedHashSet<>();
	@Getter
	private YearMonth[] revisions;
	
	private YearTableMetadata(String tablename, String revisionColumn, Map<String, ColumnMetadata> columns) {
		super(tablename, columns);
		this.revisionColumn = revisionColumn;
		this.revisions = EMPTY_REVISION;  //by default avoid NullPointerException
	}
	
	public Optional<YearMonth> latestRevision() {
		return isEmpty(revisions) ? empty() : Optional.of(revisions[0]);
	}
	
	@Override
	public void fetch() throws SQLException { //individually fetching
		try(var cn = database().getDataSource().getConnection()) {
			fetch(cn.getMetaData());
			fetchRevisions(cn);
		}
	}
	
	@Override
	void fetch(DatabaseMetaData metadata) throws SQLException  {
		tablenames.clear();
		var dbMap = getColumns().values().stream().collect(toMap(ColumnMetadata::getColumnName, ColumnMetadata::reset)); //important! reset columns
		Set<String> dirtyColumns = new LinkedHashSet<>();
		Map<String, Set<String>> columnTables = new LinkedHashMap<>();
		try(var rs = metadata.getColumns(null, null, getTablename() + "_20__", null)){
			if(!rs.next()) {
				throw new NoSuchElementException("no tables found with pattern " + getTablename() + PATTERN);
			}
			do {
				var tn = rs.getString("TABLE_NAME");
				var cm = dbMap.get(rs.getString("COLUMN_NAME"));
				if(nonNull(cm) && tn.matches(getTablename() + "_20\\d{2}")) {// untyped SQL pattern
					tablenames.add(tn);
					columnTables.computeIfAbsent(cm.getColumnName(), k-> new LinkedHashSet<>()).add(tn);
					var type = rs.getInt("DATA_TYPE");
					var size = rs.getInt("COLUMN_SIZE");
					if(cm.getDataType() == AUTO_TYPE) { //first time
						cm.setDataType(typeOf(type));
						cm.setDataSize(size);
					}
					else if(cm.getDataType().getValue() != type || cm.getDataSize() != size) {
						dirtyColumns.add(cm.getColumnName());
					}
				}//else undeclared column
			} while(rs.next());
		}
		if(!dbMap.keySet().equals(columnTables.keySet())) {
			var missingCols = dbMap.keySet().stream().filter(not(columnTables.keySet()::contains)).collect(toList());
			throw new NoSuchElementException("column(s) [" + join(", ", missingCols) + "] not found in any table " + getTablename() + PATTERN);
		}
		var missingCols = columnTables.entrySet().stream().filter(e-> !e.getValue().equals(tablenames)).map(Entry::getKey).collect(toList());
		if(!missingCols.isEmpty()) {
			throw new IllegalStateException("column(s) [" + join(", ", missingCols) + "] must be present in all tables " + getTablename() + PATTERN);
		}
		if(!dirtyColumns.isEmpty()) {
			throw new IllegalStateException("column(s) [" + join(", ", dirtyColumns) + "] must have the same definition in all tables " + getTablename() + PATTERN);
		}
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
					.map(y-> y.atMonth(DECEMBER)) //or JUNARY !?
					.toArray(YearMonth[]::new);
		}
		else {
			var query = tablenames.stream() //use JQuery
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
				this.revisions = EMPTY_REVISION; 
			}
		}
	}

	static YearTableMetadata yearTableMetadata(YearTableDecorator table, Collection<ColumnDecorator> columns) {
		return new YearTableMetadata(table.tableName(), 
				table.monthRevision().flatMap(table::columnName).orElse(null), 
				declaredColumns(table, columns));
	}
	
	static YearTableMetadata emptyMetadata(YearTableDecorator table) {
		return new YearTableMetadata(table.tableName(), 
				table.monthRevision().flatMap(table::columnName).orElse(null), 
				emptyMap());
	}
}
