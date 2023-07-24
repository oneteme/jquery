package org.usf.jquery.web;

import static java.lang.String.format;
import static java.time.Month.DECEMBER;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.UNLIMITED;
import static org.usf.jquery.core.Utils.isPresent;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class YearTableDecoratorWrapper extends TableDecoratorWrapper implements YearTableDecorator {
	
	private YearMonth[] revisions;

	public YearTableDecoratorWrapper(YearTableDecorator table) {
		super(table);
	}

	@Delegate
	@Override
	YearTableDecorator getTable() {
		return (YearTableDecorator) super.getTable();
	}
	
	@Override
	public YearMonth[] availableRevisions() {
		return isNull(getTable().availableRevisions()) ? revisions : getTable().availableRevisions();
	}
	
	@Override
	void fetch(DatabaseMetaData metadata) throws SQLException  {
		getColumns().clear();
		var decCols = declaredColumn();
		var tabCols = tableColumns(metadata, decCols);
//		tabCols.entrySet().forEach(e-> checkExistingColumns(e.getKey(), decCols));
		decCols.entrySet().forEach(e->{
			List<ColumnMetadata> colMetadatas = new LinkedList<>();
			for(var t : tabCols.entrySet()) {
				System.out.println(t.getKey() + ":" + e.getKey());
				colMetadatas.add(t.getValue().stream() //duplicate code
				.filter(c-> c.getColumnName().equals(e.getKey()))
				.findAny()
				.orElseThrow());
			}
			ColumnMetadata ref = colMetadatas.get(0);
			int i=1;
			while(i < colMetadatas.size() && colMetadatas.get(i++).equals(ref));
			if(i < colMetadatas.size()) {
				log.warn("pretty msg");
				ref = new ColumnMetadata(e.getKey(), AUTO_TYPE, UNLIMITED);
			}
			getColumns().put(e.getValue().identity(), ref);
		});
		logTableColumns(getColumns());
		this.revisions = yearMonthRevisions(metadata.getConnection(), tabCols.keySet());
		logRevisions(revisions);
	}
	
	private Map<String, List<ColumnMetadata>> tableColumns(DatabaseMetaData metadata, Map<String, ColumnDecoratorWrapper> declaredCols) throws SQLException {
		Map<String, List<ColumnMetadata>> tableCols = new LinkedHashMap<>();
		try(var rs = metadata.getColumns(null, null, tableName() + "_20__", null)){
			if(!rs.next()) {
				throw new NoSuchElementException("no tables found with pattern " + tableName() + "_[year]");
			}
			do {
				var cn = rs.getString("COLUMN_NAME");
				if(declaredCols.containsKey(cn)) {
					tableCols.computeIfAbsent(rs.getString("TABLE_NAME"), k-> new LinkedList<>())
					.add(new ColumnMetadata(cn,
							rs.getInt("DATA_TYPE"), 
							rs.getInt("COLUMN_SIZE")));
				}
				//else undeclared column
			} while(rs.next());
		}
		return tableCols;
	}
	
	private YearMonth[] yearMonthRevisions(Connection cn, Set<String> tableNames) {
		if(isNull(revisionColumn())) { //use revision year only 
			return tableNames.stream()
					.map(n-> n.substring(n.length()-4))
					.map(Year::parse)
					.sorted(reverseOrder())
					.map(y-> y.atMonth(DECEMBER))
					.toArray(YearMonth[]::new);
		}
		log.info("Scanning '{}' table month revisions...", identity());
		var rc = columnName(revisionColumn());
		var query = tableNames.stream()
		.map(t-> "SELECT DISTINCT " + rc + ", " + t.substring(t.length()-4) + " FROM " + t)
		.collect(joining(" UNION ALL ", "(", ")"));
		var yearMonths = new LinkedList<YearMonth>();
		try(var ps = cn.createStatement()){
			log.debug(query);
			try(var rs = ps.executeQuery(query)){
				while(rs.next()) {
					yearMonths.add(YearMonth.of(rs.getInt(2), rs.getInt(1)));
				}
			}
			return yearMonths.stream().sorted(reverseOrder()).toArray(YearMonth[]::new); //sort desc.
		}
		catch(SQLException e) {
			log.error(identity() + " : cannot fetch month revisions", e);
			return null; 
		}
	}

	static void logRevisions(YearMonth[] revs) {
		
		if(isPresent(revs)) {
			var pattern = "|%-5s|%-40s|";
			var bar = format(pattern, "", "").replace("|", "+").replace(" ", "-");
			var map = Stream.of(revs).collect(groupingBy(YearMonth::getYear));
			log.info(bar);
			log.info(format(pattern, "YEAR", "MONTHS"));
			log.info(bar);
			map.entrySet().stream().sorted(comparing(Entry::getKey)).forEach(e-> 
			log.info(format(pattern, e.getKey(), e.getValue().stream().map(o-> o.getMonthValue() + "").collect(joining(", ")))));
			log.info(bar);
		}
	}

}
