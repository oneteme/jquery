package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public final class ParametredQuery {
	
	@NonNull
	private final String query;
	private final String[] columnNames;
	private final Object[] params;
	private final boolean noResult;
	
	public <T> T execute(DataSource ds, ResultMapper<T> mapper){

		T res = null;
		try(var cn = ds.getConnection()){
			try(var ps = cn.prepareStatement(query)){
				log.debug("Executing prepared statement : {}", query);
				if(params != null) {
					for(var i=0; i<params.length; i++) {
						ps.setObject(i+1, params[i]);
					}						
				}
		        log.debug("Using parameters : {}", Arrays.toString(params));
				try(var rs = ps.executeQuery()){
					if(rs.next()) {
						res = mapper.apply(rs);
					}
				}
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	public List<DynamicModel> mapRows(ResultSet rs) throws SQLException {
		log.debug("Mapping results...");
		var bg = currentTimeMillis();
		var results = new LinkedList<DynamicModel>();
		while(rs.next()) {
	    	var model = new DynamicModel();
	        for(var i=0; i<columnNames.length; i++) {
	        	model.put(columnNames[i], rs.getObject(i+1));
	        }
	        results.add(model);
		}
		log.debug("{} rows mapped in {} ms", results.size(), currentTimeMillis() - bg);
        return results;
	}
	
	//not used => PG insensitive column case 
	static String[] columnNames(ResultSet rs) throws SQLException {
		var n = rs.getMetaData().getColumnCount();
		var names = new String[n];
		for(var i=0; i<n; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
		}
		return names;
	}
	
	@FunctionalInterface
	public interface ResultMapper<T> {

	    T apply(ResultSet rs) throws SQLException;
	}
	
	public boolean hasNoResult() {
		return noResult;
	}
}
