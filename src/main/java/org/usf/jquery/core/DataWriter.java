package org.usf.jquery.core;

import static java.lang.System.lineSeparator;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface 
public interface DataWriter {
	
	void write(String s) throws IOException;
	
	default void writeLine(String s) throws IOException {
		write(s);
		writeLine();
	}
	
	default void writeLine() throws IOException {
		write(lineSeparator());
	}
	
	static DataWriter consolePrinter() {
		return System.out::print;
	}

	static DataWriter toDataWriter(OutputStream out) {
		return v-> out.write(v.getBytes());
	}
	
	static DataWriter toDataWriter(Writer w) {
		return w::write;
	}
	
	static RowWriter debugLogger() {
		return rowWriter(getLogger(DataWriter.class)::debug);
	}

	static RowWriter infoLogger() {
		return rowWriter(getLogger(DataWriter.class)::info);
	}
	
	static RowWriter rowWriter(Consumer<String> writer) {
		return new RowWriter(writer);
	}
	
	@RequiredArgsConstructor
	final class RowWriter implements DataWriter {
		
		private final StringBuilder sb = new StringBuilder();
		private final Consumer<String> writer;
		
		@Override
		public void write(String s) throws IOException {
			sb.append(s);
		}

		@Override
		public void writeLine() throws IOException {
			writeLine("");
		}
		
		@Override
		public void writeLine(String s) throws IOException {
			writer.accept(sb.append(s).toString());
			sb.setLength(0); //clear
		}
	}
}