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

/**
 * 
 * @author u$f
 *
 */
public interface QueryExecutor2 {

	static final Logger log = getLogger(QueryExecutor.class);
	
	default <T> T execute(SqlQuery query, ResultSetMapper<T> mapper, DataSource ds) {
		log.debug("preparing query: {}", query.sql());
		try(var cnx = ds.getConnection(); 
			var ps = cnx.prepareStatement(query.sql())){
			var args = query.args();
			if(!isEmpty(args)) {
				if(log.isDebugEnabled()) {
					log.debug("binding query parameters: {}", Arrays.toString(values(args)));
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
			var ct = currentTimeMillis();
			try(var rs = ps.executeQuery()){
				if(log.isDebugEnabled()) {	
					log.debug("query executed in {} ms", currentTimeMillis() - ct);
				}
				return mapper.mapUnchecked(rs);
			}
		}
		catch (SQLException e) {
			var args = values(query.args());
			throw new QueryExecutionException("error executing query: " + query.sql() + " with args: " + Arrays.toString(args), e);
		}
	}
}
