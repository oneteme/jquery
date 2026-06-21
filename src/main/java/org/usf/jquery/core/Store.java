package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.usf.jquery.core.Stores.getCurrentStore;
import static org.usf.jquery.core.Stores.setCurrentStore;
import static org.usf.jquery.core.TypedArg.values;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.slf4j.Logger;

/**
 * 
 * @author u$f
 *
 */
public interface Store {

	static final Logger log = getLogger(QueryExecutor.class);

	String name();

	Dialect dialect();
	
	DataSource dataSource();
	
	default Table table(String name) {
		return new Table(name, name());
	}
	
	default Query newQuery(Consumer<QueryComposer> cons) {
		var qc = new QueryComposer();
		var cs = getCurrentStore();
		if(isNull(cs)) {
			setCurrentStore(this);
			try {
				cons.accept(qc);
			}
			finally {
				setCurrentStore(null);
			}
		}
		else if(cs == this) {
			cons.accept(qc); //nested query
		}
		else {
			throw new IllegalStateException("store mismatch");
		}
		return qc.compose(this);
	}

	default <T> T execute(Query query, ResultSetMapper<T> mapper) {
		log.debug("building query");
		var sqlQuery = query.build();
		log.debug("preparing query: {}", sqlQuery.sql());
		try(var cnx = dataSource().getConnection(); 
			var ps = cnx.prepareStatement(sqlQuery.sql())){
			var args = sqlQuery.args();
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
			var args = values(sqlQuery.args());
			throw new QueryExecutionException("error executing query: " + sqlQuery.sql() + " with args: " + Arrays.toString(args), e);
		}
	}
}
