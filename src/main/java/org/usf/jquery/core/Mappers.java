package org.usf.jquery.core;

import static org.usf.jquery.core.ResultSetMapper.DataWriter.usingRowWriter;

import java.io.Writer;
import java.util.List;
import java.util.function.IntFunction;

import org.usf.jquery.core.ResultSetMapper.DataWriter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {
	
	public static KeyValueMapper keyValue() {
		return new KeyValueMapper();
	}
	
	public static AsciiResultMapper log() {
		return new AsciiResultMapper(usingRowWriter(log::debug));
	}
	
	public static AsciiResultMapper ascii(Writer w) {
		return ascii(w::write);
	}
	
	public static AsciiResultMapper ascii(DataWriter out) {
		return new AsciiResultMapper(out);
	}
	
	public static CsvResultMapper csv(Writer w) {
		return csv(w::write);
	}
	
	public static CsvResultMapper csv(DataWriter out) {
		return new CsvResultMapper(out);
	}
	
	public static <T> ResultSetMapper<T[]> toArray(RowMapper<T> mapper, IntFunction<T[]> fn) {
		return rs-> mapper.map(rs).toArray(fn);
	}
	
	public static <T> ResultSetMapper<List<T>> toList(RowMapper<T> mapper) {
		return mapper;
	}
}
