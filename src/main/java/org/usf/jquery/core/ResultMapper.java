package org.usf.jquery.core;


import static java.lang.System.lineSeparator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.usf.jquery.core.ResultMapper.RowWriter;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ResultMapper<T> {
	
    T map(ResultSet rs) throws SQLException;
	
	default String[] declaredColumns(ResultSet rs) throws SQLException {
		var names = new String[rs.getMetaData().getColumnCount()];
		for(var i=0; i<names.length; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
		}
		return names;
	}

	@FunctionalInterface
	interface DataWriter {
		
		void write(String s) throws IOException;
		
		default void writeLine(String s) throws IOException {
			write(s);
			writeLine();
		}
		
		default void writeLine() throws IOException {
			write(lineSeparator());
		}
		
		static DataWriter usingRowWriter(Consumer<String> writer) {
			return new RowWriter(writer);
		}
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
			this.sb.delete(0, sb.length()); //clear
		}

	}
}