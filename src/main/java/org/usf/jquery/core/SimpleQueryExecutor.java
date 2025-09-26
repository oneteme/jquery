package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.TypedArg.values;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class SimpleQueryExecutor<T> implements QueryExecutor<T> {
	
	private final ResultSetMapper<T> mapper;
	
	@Override
	public T execute(Query query, DataSource ds) {
		try {
			try(var cn = ds.getConnection()){
				return execute(query, cn);
			}
		}
		catch (SQLException e) {
			throw new JQueryException(e);
		}	
	}

	public T execute(Query query, Connection cnx) throws SQLException {
		var sql = query.getSql();
		log.debug("preparing statement : {}", sql);
		try(var ps = cnx.prepareStatement(sql)){
			var args = query.getArgs();
			if(!isEmpty(args)) {
				log.debug("using arguments : {}", Arrays.toString(values(args)));
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
					throw new MappingException("error mapping results for query: " + sql, e);
				}
			}
		}
	}
}
