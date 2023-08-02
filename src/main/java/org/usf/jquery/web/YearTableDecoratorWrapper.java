package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.TableMetadata.requireSameStructure;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class YearTableDecoratorWrapper extends TableDecoratorWrapper implements YearTableDecorator {
	
	private Set<String> tablenames;
	private YearMonth[] revisions;

	public YearTableDecoratorWrapper(YearTableDecorator table) {
		super(table);
	}
	
	@Override
	public ColumnDecorator revisionColumn() {
		return wrappedYearTable().revisionColumn();
	}
	
	@Override
	public YearMonth[] availableRevisions() {
		return ofNullable(wrappedYearTable().availableRevisions())
				.orElse(revisions);// can also be null 
	}
	
	@Override
	void fetch(DatabaseMetaData metadata) throws SQLException  {
		tableMetadata.clear();
		var declaredCols = declaredColumns();
		Map<String, TableMetadata> tables = new LinkedHashMap<>();
		try(var rs = metadata.getColumns(null, null, tableName() + "_20__", null)){
			if(!rs.next()) {
				throw new NoSuchElementException("no tables found with pattern " + tableName() + "_20xx");
			}
			do {
				var cn = rs.getString("COLUMN_NAME");
				if(declaredCols.containsKey(cn)) {
					tables.computeIfAbsent(rs.getString("TABLE_NAME"), TableMetadata::new)
					.put(declaredCols.get(cn), new ColumnMetadata(cn,
							rs.getInt("DATA_TYPE"), 
							rs.getInt("COLUMN_SIZE")));
				}
				//else undeclared column
			} while(rs.next());
		}
		tables.values().forEach(tm-> tm.requireColumns(this, declaredCols.values()));
		requireSameStructure(declaredCols.values(), tables.values());
		this.tableMetadata.putAll(tables.values().iterator().next().getColumns());
		this.tablenames = tables.keySet();
	}
	
	public void fetchRevisions(Connection cn) { // change this call
		if(isEmpty(tablenames)) {
			return;
		}
		if(isNull(revisionColumn())) { //revision year only 
			this.revisions = tablenames.stream()
					.map(n-> n.substring(n.length()-4))
					.map(Year::parse)
					.sorted(reverseOrder())
					.map(y-> y.atMonth(DECEMBER))
					.toArray(YearMonth[]::new);
		}
		else {
			var rc = columnName(revisionColumn())
					.orElseThrow(()-> new IllegalStateException("revision column '"+ revisionColumn().identity() + "' must be declared in '" + identity() + "' table"));
			var query = tablenames.stream()
			.map(t-> "SELECT DISTINCT " + rc + ", " + t.substring(t.length()-4) + " FROM " + t)
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

	YearTableDecorator wrappedYearTable() {
		return (YearTableDecorator) super.wrappedTable;
	}	
}
