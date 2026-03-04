package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.usf.jquery.core.TypedArg.values;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.slf4j.Logger;

public interface QueryExecutor2 {

	static final Logger log = getLogger(QueryExecutor.class);
	
	default <T> T execute(Query query, ResultSetMapper<T> mapper, DataSource ds) {
		try {
			return internalExecute(query, mapper, ds);
		}
		catch (Exception e) {
			throw new RuntimeException("error executing query: " + query.getSql(), e);
		}
	}
	
	default <T> T internalExecute(Query query, ResultSetMapper<T> mapper, DataSource ds) throws SQLException {
		log.debug("preparing query : {}", query.getSql());
		try(var cnx = ds.getConnection(); 
			var ps = cnx.prepareStatement(query.getSql())){
			var args = query.getArgs();
			if(!isEmpty(args)) {
				if(log.isDebugEnabled()) {
					log.debug("using arguments : {}", Arrays.toString(values(args)));
				}
				for(var i=0; i<args.length; i++) {
					if(isNull(args[i].value())) {
						ps.setNull(i+1, args[i].type());
					}
					else {
						ps.setObject(i+1, args[i].value(), args[i].type());
					}
				}						
			}
			log.trace("executing query..");
			var bg = currentTimeMillis();
			try(var rs = ps.executeQuery()){
				log.trace("query executed in {} ms", currentTimeMillis() - bg);
				try {
					return mapper.map(rs);
				}
				catch(SQLException e) {
					throw new MappingException("error mapping results for query: " + query.getSql(), e);
				}
			}
		}
	}
}
