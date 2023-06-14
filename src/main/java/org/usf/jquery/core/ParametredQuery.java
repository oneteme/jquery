package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

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
	private final String[] columnNames;
	private final Object[] params;
	private final boolean noResult;
	
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
		        mapper.declaredColumns(columnNames);
				try(var rs = ps.executeQuery()){
					return rs.next() ? mapper.map(rs) : null;
				}
			}
		}
		catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ResultSimpleMapper defaultMapper() {
		var mapper = new ResultSimpleMapper();
		mapper.declaredColumns(columnNames);
		return mapper;
	}
	
	//not used => PG insensitive column case 
	static String[] columnNames(ResultSet rs) throws SQLException {
		var names = new String[rs.getMetaData().getColumnCount()];
		for(var i=0; i<names.length; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
		}
		return names;
	}
	
	public boolean hasNoResult() {
		return noResult;
	}
}
