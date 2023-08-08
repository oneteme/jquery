package org.usf.jquery.core;

import static org.usf.jquery.core.ResultMapper.RowWriter.lineSeparator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.usf.jquery.core.ResultMapper.RowWriter;

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
		return execute(ds, new ResultSimpleMapper());
	}
	
	public <T> T execute(DataSource ds, ResultMapper<T> mapper) {
		try(var cn = ds.getConnection()){
			log.info("preparing statement : {}", query);
			try(var ps = cn.prepareStatement(query)){
				if(params != null) {
					for(var i=0; i<params.length; i++) {
						ps.setObject(i+1, params[i]);
					}						
				}
		        log.info("with parameters : {}", Arrays.toString(params));
				try(var rs = ps.executeQuery()){
					return mapper.map(rs);
				}
			}
		}
		catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	
	public void toCsv(DataSource ds, Writer w) {
		toCsv(ds, lineSeparator(w::write));
	}

	public void toCsv(DataSource ds, RowWriter out) {
		execute(ds, new ResultCsvExport(out));
	}

	public void toAscii(DataSource ds, Writer w) {
		toAscii(ds, lineSeparator(w::write));
	}
	
	public void toAscii(DataSource ds, RowWriter out) {
		execute(ds, new ResultAsciiExport(out));
	}
	
	public void debugResult(DataSource ds) {
		execute(ds, new ResultAsciiExport(log::debug));
	}
	
	public ResultSimpleMapper defaultMapper() {
		return new ResultSimpleMapper();
	}
}
