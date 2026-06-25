package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.usf.jquery.core.Stores.getCurrentDialect;
import static org.usf.jquery.core.Stores.setCurrentDialect;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.SQLException;
import java.util.List;
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
	
	default TypeConverterRegistry typeConverters(){
		return new TypeConverterRegistry();
	}
	
	default Table table(String name) {
		return new Table(name, name());
	}
	
	default Query newQuery(Consumer<QueryComposer> cons) {
		var qc = new QueryComposer();
		var cs = getCurrentDialect();
		if(isNull(cs)) {
			setCurrentDialect(dialect());
			try {
				cons.accept(qc);
			}
			finally {
				setCurrentDialect(null);
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

	@SuppressWarnings("unchecked")
	default <T> T execute(Query query, ResultSetMapper<T> mapper) {
		log.debug("building query");
		var sqlQuery = query.build();
		log.debug("preparing query: {}", sqlQuery.sql());
		try(var cnx = dataSource().getConnection(); 
			var ps = cnx.prepareStatement(sqlQuery.sql())){
			var args = sqlQuery.args();
			if(!isEmpty(args)) {
				if(log.isDebugEnabled()) {
					log.debug("binding query parameters: {}", values(args));
				}
				var registry = typeConverters();
				for(var i=0; i<args.size(); i++) {
					var arg = args.get(i);
					if(isNull(arg.value())) {
						ps.setNull(i+1, arg.type().getValue());
					}
					else {
						var cnv = (TypeConverter<Object>)registry.getConverter(arg.value().getClass());
						var val = nonNull(cnv) ? cnv.convert(arg.value(), arg.type()) : arg.value(); //TODO trace
						ps.setObject(i+1, val, arg.type().getValue());
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
			throw new QueryExecutionException("error executing query: " + sqlQuery.sql() + " with args: " + values(sqlQuery.args()), e);
		}
	}
	
	public static List<Object> values(List<TypedArg> arr) {
		return nonNull(arr) ? arr.stream().map(TypedArg::value).toList() : null;
	} 
}
