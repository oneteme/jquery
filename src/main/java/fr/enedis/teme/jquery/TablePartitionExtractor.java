package fr.enedis.teme.jquery;

import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TablePartitionExtractor {
	
	private final DataSource ds;
	private final DBTable[] tables;
	private Map<DBTable, List<YearMonth>> cache;
	
	public YearMonth lastRevision(DBTable table) {
		var revs = requireTablePartition(table);
		return revs.get(0); //list already sorted desc.
	}

	public YearMonth firstRevision(DBTable table) {
		var revs = requireTablePartition(table);
		return revs.get(revs.size()-1); //list already sorted desc.
	}
	
	public YearMonth lastRevisionBetween(DBTable table, YearMonth min, YearMonth max) {
		var revs = requireTablePartition(table);
		return revs.stream()
				.filter(v-> v.compareTo(min) >=0 && v.compareTo(max) <=0)
				.findFirst() //list already sorted desc.
				.orElseThrow(()-> new PartitionExtractException(table + ": empty table partition between [" + min + ", " + max + "]"));
	}
	
	private List<YearMonth> requireTablePartition(DBTable table) {
		var revs = fetch(false).get(table);
		if(revs == null || revs.isEmpty()) {
			throw new PartitionExtractException(table + ": empty table partition");
		}
		return revs;
	}

	private Map<DBTable, List<YearMonth>> fetch(boolean refresh) {
		synchronized (ds) {
			if(cache == null || refresh) {
				cache = Stream.of(tables).collect(toUnmodifiableMap(identity(), this::tableMonths));
			}
			return cache;
		}		
	}
	
	private List<YearMonth> tableMonths(DBTable table) {
		
		List<String> l = new LinkedList<>();
		try(ResultSet rs = ds.getConnection().getMetaData().getTables(null, null, table.getTableName()+"_20__", null)){
			while(rs.next()) {
				var tn = rs.getString("TABLE_NAME");
				if(tn.matches(table.getTableName() + "_20[0-9]{2}")) { // strict pattern
					l.add(tn);
				}
			}
		}
		catch(SQLException e) {
			throw new PartitionExtractException(table + " : cannot extract table partition years : ", e);
		}
		return tableMonths(table, l);
	}
	
	private List<YearMonth> tableMonths(DBTable table, List<String> tableName) {
		
		var rc = table.getColumnName(table.getRevisionColumn());
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
			throw new PartitionExtractException(table + " : cannot extract table partition months : ", e);
		}
		return months.stream().sorted(reverseOrder()).collect(toList()); //sort desc.
	}
	
	public static TablePartitionExtractor extract(DataSource ds, DBTable... tables) {
		
		return new TablePartitionExtractor(requireNonNull(ds), requireNonNull(tables));
	}

}
