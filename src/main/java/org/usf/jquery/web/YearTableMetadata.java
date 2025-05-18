package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.Month.DECEMBER;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.YearViewDecorator.EMPTY_REVISION;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.TableView;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class YearTableMetadata extends ViewMetadata {
	
	private static final String PATTERN = "_20**";
	
	private final String revisionColumn; //optional
	private final Set<String> tablenames = new LinkedHashSet<>();
	@Getter
	private YearMonth[] revisions;
	
	YearTableMetadata(ViewDecorator view, String revisionColumn, Map<String, ColumnMetadata> columns) {
		super(view, columns);
		this.revisionColumn = revisionColumn;
		this.revisions = EMPTY_REVISION;  //by default avoid NullPointerException
	}
	
	public Optional<YearMonth> latestRevision() {
		return isEmpty(revisions) ? empty() : Optional.of(revisions[0]);
	}
		
	@Override
	void fetchView(DatabaseMetaData metadata, TableView view, String schema) throws SQLException  {
		tablenames.clear();
		var dbMap = getColumns().values().stream().collect(toMap(cm-> cm.getName(), ColumnMetadata::reset)); //important! reset columns
		Set<String> dirtyColumns = new LinkedHashSet<>();
		Map<String, Set<String>> columnTables = new LinkedHashMap<>();
		try(var rs = metadata.getColumns(null, view.getSchemaOrElse(schema), view.getName() + "_20__", null)){
			if(!rs.next()) {
				throw new NoSuchElementException("no tables found with pattern " + view + PATTERN);
			}
			do {
				var tn = rs.getString("TABLE_NAME");
				var cm = dbMap.get(rs.getString("COLUMN_NAME"));
				if(nonNull(cm) && tn.matches(view.getName() + "_20\\d{2}")) {// untyped SQL pattern
					tablenames.add(tn);
					columnTables.computeIfAbsent(cm.getName(), k-> new LinkedHashSet<>()).add(tn);
					if(!cm.isOverConfigured()) {
						var type = rs.getInt("DATA_TYPE");
						var size = rs.getInt("COLUMN_SIZE");
						var digt = rs.getInt("DECIMAL_DIGITS");
						if(isNull(cm.getType())) { //first time
							cm.update(type, size, digt);
						}
						else if(cm.getType().getValue() != type || cm.getDataSize() != size || cm.getPrecision() != digt) {
							dirtyColumns.add(cm.getName());
						}
					}
				}//else undeclared column
			} while(rs.next());
		}
		if(!dbMap.keySet().equals(columnTables.keySet())) {
			var missingCols = dbMap.keySet().stream().filter(not(columnTables.keySet()::contains)).toList();
			throw new NoSuchElementException("column(s) [" + join(", ", missingCols) + "] not found in any table " + view + PATTERN);
		}
		var missingCols = columnTables.entrySet().stream().filter(e-> !e.getValue().equals(tablenames)).map(Entry::getKey).toList();
		if(!missingCols.isEmpty()) {
			throw new IllegalStateException("column(s) [" + join(", ", missingCols) + "] must be present in all tables " + view + PATTERN);
		}
		if(!dirtyColumns.isEmpty()) {
			throw new IllegalStateException("column(s) [" + join(", ", dirtyColumns) + "] must have the same definition in all tables " + view + PATTERN);
		}
	}
	
	@Override
	void fetch(DatabaseMetaData metadata, DBView qr, String schema) throws SQLException {
		throw new UnsupportedOperationException("query");
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
		logRevisions();
	}
	
	void logRevisions() {
		if(!isEmpty(revisions)) {
			var pattern = "|%-5s|%-40s|";
			var bar = format(pattern, "", "").replace("|", "+").replace(" ", "-");
			var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
			log.info(bar);
			log.info(format(pattern, "YEAR", "MONTHS"));
			log.info(bar);
			map.entrySet().stream().sorted(comparing(Entry::getKey)).forEach(e-> 
			log.info(format(pattern, e.getKey(), e.getValue().stream().map(o-> o.getMonthValue() + "").collect(joining(", ")))));
			log.info(bar);
		}
	}
}
