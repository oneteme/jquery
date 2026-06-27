package org.usf.jquery.core;

import static java.util.Collections.emptyList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {

	public static KeyValueMapper keyValueMapper() {
		return new KeyValueMapper();
	}

	public static AsciiResultMapper asciiWriter(DataWriter writer) {
		return new AsciiResultMapper(writer);
	}

	public static CsvResultMapper csvWriter(DataWriter writer) {
		return new CsvResultMapper(writer);
	}
	
	public static <T> ResultSetMapper<T> toSingleRowMapper(RowMapper<T> rm) {
		return rs-> rs.next() ? rm.mapRow(rs, rs.getRow()) : null;
	}

	public static <T> ResultSetMapper<T[]> toArrayMapper(RowMapper<T> rm, IntFunction<T[]> fn) {
		return rs-> toListMapper(rm).map(rs).toArray(fn);
	}

	public static <T> ResultSetMapper<List<T>> toListMapper(RowMapper<T> rm) {
		return toListMapper(rm, ArrayList::new);
	}

	public static <T> ResultSetMapper<List<T>> toListMapper(RowMapper<T> rm, Supplier<List<T>> supplier) {
		return rs->{
			if(rs.next()) {
				var arr = supplier.get();
				while(arr.add(rm.mapRow(rs, rs.getRow())) && rs.next());
				return arr;
			}
			return emptyList(); //avoids creating an List if the ResultSet is empty
		};
	}
	
	public static <T> ResultSetMapper<T> resultSetLimiter(ResultSetMapper<T> rm, int limit){
		if(limit > 0) {
			return rs-> rm.map(new ResultSetLimiter(rs, limit));
		}
		throw new IllegalArgumentException("limit must be >= 0");
	}
	
	@RequiredArgsConstructor
	static final class ResultSetLimiter implements ResultSet {

		@Delegate
		private final ResultSet rs;
		private final int limit;
		
		@Override
		public boolean next() throws SQLException {
			var nxt = rs.next();
			if(nxt && rs.getRow() > limit) {
				throw new LimitExceededException("ResultSet limit exceeded: " + limit);
			}
			return nxt;
		}
	}
}
