package org.usf.jquery.core;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

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
public final class ParametredQuery {
	
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
		        log.info("using parameters : {}", Arrays.toString(params));
				try(var rs = ps.executeQuery()){
					return mapper.map(rs);
				}
			}
		}
		catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ResultSimpleMapper defaultMapper() {
		return new ResultSimpleMapper();
	}
}
