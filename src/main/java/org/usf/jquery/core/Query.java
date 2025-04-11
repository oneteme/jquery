package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.Connection;
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
public final class Query {
	
	@NonNull
	private final String sql;
	private final TypedArg[] args;
	
	public List<DynamicModel> execute(DataSource ds) throws SQLException {
		return execute(ds, new KeyValueMapper());
	}
	
	public <T> T execute(DataSource ds, ResultSetMapper<T> mapper) throws SQLException {
		try(var cn = ds.getConnection()){
			return execute(cn, mapper);
		}
	}
	
	public <T> T execute(Connection cn, ResultSetMapper<T> mapper) throws SQLException {
		log.debug("preparing statement : {}", sql);
		log.debug("using arguments : {}", Arrays.toString(args)); //before prepare
		try(var ps = cn.prepareStatement(sql)){
			if(!isEmpty(args)) {
				for(var i=0; i<args.length; i++) {
					if(isNull(args[i].value())) {
						ps.setNull(i+1, args[i].type());
					}
					else {
						ps.setObject(i+1, args[i].value(), args[i].type());
					}
				}						
			}
	        log.trace("executing SQL query...");
	        var bg = currentTimeMillis();
			try(var rs = ps.executeQuery()){
		        log.trace("query executed in {} ms", currentTimeMillis() - bg);
		        try {
		        	return mapper.map(rs);
		        }
				catch(SQLException e) {
					throw new MappingException("error while mapping results", e);
				}
			}
		}
	}
}
