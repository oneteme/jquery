package org.usf.jquery.core;

import static org.usf.jquery.core.ResultSetMapper.DataWriter.usingRowWriter;

import java.io.Writer;

import org.usf.jquery.core.ResultSetMapper.DataWriter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {
	
	public KeyValueMapper keyValue() {
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
	
}
