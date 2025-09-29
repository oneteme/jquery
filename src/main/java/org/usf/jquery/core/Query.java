package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.TypedArg.values;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

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

	@NonNull private final Environment env;
	@NonNull private final String sql;
	private final TypedArg[] args; //optional if !prepared

	public <T> T execute(ResultSetMapper<T> mapper) {
		try {
			try(var cn = requireNonNull(env.getDataSource(), "require datasource").getConnection()){
				return execute(mapper, cn);
			}
		}
		catch (SQLException e) {
			throw new JQueryException(e);
		}	
	}

	public <T> T execute(ResultSetMapper<T> mapper, Connection cnx) throws SQLException {
		log.debug("preparing statement : {}", sql);
		try(var ps = cnx.prepareStatement(sql)){
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
 
	@Override
	public String toString() {
		return sql;
	}
}
