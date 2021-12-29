package fr.enedis.teme.jquery;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.reverseOrder;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TablePartitionScanner implements BiPredicate<DBTable, YearMonth> {
	
	private final DataSource ds;
	private final DBTable[] tables;
	private final DBColumn revColumn;
	private final Future<?> sync;
	private Map<DBTable, List<YearMonth>> cache = emptyMap();

	public TablePartitionScanner(DataSource ds, DBTable[] tables, DBColumn revColumn, int refreshDelay) {
		this.ds = ds;
		this.tables = tables;
		this.revColumn = revColumn;
		this.sync = newScheduledThreadPool(1)
				.scheduleWithFixedDelay(()-> this.fetch(true), 1, refreshDelay, MINUTES); //fetch every hour
	}
	
	@Override
	public boolean test(DBTable table, YearMonth ym) {
		var revs = fetch(false).get(table);
		if(revs == null || revs.isEmpty()) {
			return true; //cannot fetch data => force true
		}
		//sorted already
		return revs.get(0).compareTo(ym) >= 0 && 
				revs.get(revs.size()-1).compareTo(ym) <= 0;
	}

	private Map<DBTable, List<YearMonth>> fetch(boolean refresh) {
		synchronized (ds) {
			if(cache.isEmpty() || refresh) {
				cache = Stream.of(tables).collect(toUnmodifiableMap(identity(), this::tableMonths));
			}
			return cache;
		}		
	}
	
	private List<YearMonth> tableMonths(DBTable table) {
		
		List<String> nName = new LinkedList<>();
		try(ResultSet rs = ds.getConnection().getMetaData().getTables(null, null, table.getTableName()+"_20__", null)){
			while(rs.next()) {
				var tn = rs.getString("TABLE_NAME");
				if(tn.matches(table.getTableName() + "_20[0-9]{2}")) { // strict pattern
					nName.add(tn);
				}
			}
		}
		catch(SQLException e) {
			log.warn(table + " : cannot fetch table revision", e);
		}
		return tableMonths(table, nName);
	}
	
	private List<YearMonth> tableMonths(DBTable table, List<String> tableName) {
		
		var rc = revColumn.sql(table, null);
		var query = tableName.stream()
			.map(tn-> "SELECT DISTINCT " + rc + ", " + tn.substring(tn.length()-4) + " FROM " + tn)
			.collect(Collectors.joining("; "));
		var months = new LinkedList<YearMonth>();
		try(var cn = ds.getConnection()){
			try(var ps = cn.createStatement()){
				log.info(query);
				boolean res = ps.execute(query);
				if(res) {
					do {
						try(var rs = ps.getResultSet()){
							while(rs.next()) {
								months.add(YearMonth.of(rs.getInt(2), rs.getInt(1)));
							}
						}
						res = ps.getMoreResults();
					} while(res);
				}
			}
		}
		catch(SQLException e) {
			log.warn(table + " : cannot fetch table month revision", e);
		}
		return months.stream().sorted(reverseOrder()).collect(toList()); //sort desc.
	}
	
	public static TablePartitionScanner revisionScanner(int scanDelay, @NonNull DataSource ds, @NonNull DBColumn revColumn, @NonNull DBTable... tables) {
		return new TablePartitionScanner(ds, tables, revColumn, scanDelay);
	}
	
	public static TablePartitionScanner revisionScanner(@NonNull DataSource ds, @NonNull DBColumn revColumn, @NonNull DBTable... tables) {
		return new TablePartitionScanner(ds, tables, revColumn, 60);
	}
}
