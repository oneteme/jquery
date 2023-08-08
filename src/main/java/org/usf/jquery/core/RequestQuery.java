package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static org.usf.jquery.core.ResultMapper.DataWriter.usingRowWriter;

import java.io.Writer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.usf.jquery.core.ResultMapper.DataWriter;
import org.usf.jquery.web.Chart;
import org.usf.jquery.web.Table;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public final class RequestQuery {
	
	@NonNull
	private final String query;
	private final Object[] params;
	
	public List<DynamicModel> execute(DataSource ds) {
		return execute(ds, new SimpleResultMapper());
	}
	
	public <T> T execute(DataSource ds, ResultMapper<T> mapper) {
		try(var cn = ds.getConnection()){
			log.debug("preparing statement : {}", query);
			try(var ps = cn.prepareStatement(query)){
				if(params != null) {
					for(var i=0; i<params.length; i++) {
						ps.setObject(i+1, params[i]);
					}						
				}
		        log.debug("with parameters : {}", Arrays.toString(params));
		        log.debug("executing SQL query...");
		        var bg = currentTimeMillis();
				try(var rs = ps.executeQuery()){
			        log.debug("query executed in {} ms", currentTimeMillis() - bg);
					return mapper.map(rs);
				}
			}
		}
		catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/* experimental */
	
	public void toCsv(DataSource ds, Writer w) {
		toCsv(ds, w::write);
	}

	public void toCsv(DataSource ds, DataWriter out) {
		execute(ds, new CsvResultMapper(out));
	}

	public void toAscii(DataSource ds, Writer w) {
		toAscii(ds, w::write);
	}
	
	public void toAscii(DataSource ds, DataWriter out) {
		execute(ds, new AsciiResultMapper(out));
	}
	
	public void toChart(DataSource ds, Writer w) {
		execute(ds, new Chart(w, "host", "countRows"));
	}
	
	public void toTable(DataSource ds, Writer w) {
		execute(ds, new Table(w));
	}
	
	public void logResult(DataSource ds) {
		execute(ds, new AsciiResultMapper(usingRowWriter(log::debug)));
	}
	
	public SimpleResultMapper defaultMapper() {
		return new SimpleResultMapper();
	}
}
